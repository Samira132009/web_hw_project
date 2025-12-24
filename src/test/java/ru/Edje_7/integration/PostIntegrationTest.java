package ru.Edje_7.integration;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.Edje_7.dto.request.LoginRequest;
import ru.Edje_7.dto.request.PostRequest;
import ru.Edje_7.dto.response.ApiResponse;
import ru.Edje_7.dto.response.AuthResponse;
import ru.Edje_7.dto.response.PostResponse;
import ru.Edje_7.entity.Role;
import ru.Edje_7.entity.User;
import ru.Edje_7.repository.RoleRepository;
import ru.Edje_7.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class PostIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;
    private String token;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";

        // Create test user and roles
        createTestUser();

        // Login to get token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        ResponseEntity<ApiResponse<AuthResponse>> loginResponse = restTemplate.postForEntity(
                baseUrl + "/auth/login",
                loginRequest,
                (Class<ApiResponse<AuthResponse>>) (Class<?>) ApiResponse.class
        );

        assertNotNull(loginResponse.getBody());
        assertTrue(loginResponse.getBody().isSuccess());
        token = loginResponse.getBody().getData().getToken();
    }

    private void createTestUser() {
        // Create roles if not exist
        Optional<Role> userRole = roleRepository.findByName(Role.RoleName.ROLE_USER);
        if (userRole.isEmpty()) {
            Role role = new Role(Role.RoleName.ROLE_USER, "Regular user");
            roleRepository.save(role);
        }

        // Create test user if not exist
        if (!userRepository.existsByUsername("testuser")) {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setPasswordHash(passwordEncoder.encode("password123"));
            user.setEnabled(true);
            user.setLocked(false);

            Role role = roleRepository.findByName(Role.RoleName.ROLE_USER).get();
            user.addRole(role);

            userRepository.save(user);
        }
    }

    @Test
    void createPost_shouldReturnCreatedPost() {
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle("Integration Test Post");
        postRequest.setContent("This is an integration test post content");
        postRequest.setTags(Set.of("test", "integration"));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<PostRequest> request = new HttpEntity<>(postRequest, headers);

        ResponseEntity<ApiResponse<PostResponse>> response = restTemplate.postForEntity(
                baseUrl + "/posts",
                request,
                (Class<ApiResponse<PostResponse>>) (Class<?>) ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals("Integration Test Post", response.getBody().getData().getTitle());
    }

    @Test
    void getAllPosts_shouldReturnPosts() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/posts",
                ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void searchPosts_shouldReturnSearchResults() {
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                baseUrl + "/posts/search?q=test",
                ApiResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }
}