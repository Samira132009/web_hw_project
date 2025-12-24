package ru.Edje_7.service;


import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Edje_7.dto.request.LoginRequest;
import ru.Edje_7.dto.request.RegisterRequest;
import ru.Edje_7.dto.response.AuthResponse;
import ru.Edje_7.dto.response.UserResponse;
import ru.Edje_7.entity.Role;
import ru.Edje_7.entity.User;
import ru.Edje_7.exceptions.UnauthorizedException;
import ru.Edje_7.repository.RoleRepository;
import ru.Edje_7.repository.UserRepository;
import ru.Edje_7.security.JwtService;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            user.updateLastLogin();
            userRepository.save(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String jwt = jwtService.generateToken(userDetails);
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtService.getExpirationTime() / 1000);

            log.info("User logged in: {}", user.getUsername());

            return AuthResponse.builder()
                    .token(jwt)
                    .expiresAt(expiresAt)
                    .user(convertToUserResponse(user))
                    .build();

        } catch (Exception e) {
            log.error("Login failed for user: {}", request.getUsernameOrEmail());
            throw new UnauthorizedException("Invalid username/email or password");
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setLocked(false);

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(Role.RoleName.ROLE_USER);
                    return roleRepository.save(newRole);
                });

        user.addRole(userRole);
        User savedUser = userRepository.save(user);

        UserDetails userDetails = createUserDetails(savedUser);
        String jwt = jwtService.generateToken(userDetails);

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtService.getExpirationTime() / 1000);

        log.info("User registered: {}", savedUser.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .expiresAt(expiresAt)
                .user(convertToUserResponse(savedUser))
                .build();
    }

    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .accountExpired(false)
                .credentialsExpired(false)
                .authorities(user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .toArray(SimpleGrantedAuthority[]::new))
                .build();
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && user.isAdmin();
    }

    public boolean isModerator() {
        User user = getCurrentUser();
        return user != null && user.isModerator();
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setEmailVerified(user.getEmailVerified());

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(java.util.stream.Collectors.toSet());
        response.setRoles(roles);

        return response;
    }
}