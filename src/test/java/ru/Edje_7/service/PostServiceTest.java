package ru.Edje_7.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.Edje_7.dto.request.PostRequest;
import ru.Edje_7.dto.response.PostResponse;
import ru.Edje_7.entity.Post;
import ru.Edje_7.entity.User;
import ru.Edje_7.exceptions.ResourceNotFoundException;
import ru.Edje_7.exceptions.UnauthorizedException;
import ru.Edje_7.repository.PostRepository;
import ru.Edje_7.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setAuthor(testUser);
        testPost.setStatus(Post.Status.PUBLISHED);
        testPost.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getPostById_shouldReturnPost_whenPostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        PostResponse response = postService.getPostById(1L);

        assertNotNull(response);
        assertEquals("Test Post", response.getTitle());
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void getPostById_shouldThrowException_whenPostNotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostById(1L);
        });
    }

    @Test
    void createPost_shouldCreateNewPost() {
        PostRequest request = new PostRequest();
        request.setTitle("New Post");
        request.setContent("New Content");
        request.setTags(Set.of("java", "spring"));

        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostResponse response = postService.createPost(request, testUser);

        assertNotNull(response);
        assertEquals("Test Post", response.getTitle());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePost_shouldUpdatePost_whenUserIsAuthor() {
        PostRequest request = new PostRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostResponse response = postService.updatePost(1L, request, testUser);

        assertNotNull(response);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowException_whenUserNotAuthor() {
        PostRequest request = new PostRequest();
        request.setTitle("Updated Title");

        User otherUser = new User();
        otherUser.setId(2L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertThrows(UnauthorizedException.class, () -> {
            postService.updatePost(1L, request, otherUser);
        });
    }

    @Test
    void getAllPosts_shouldReturnPageOfPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(testPost));

        when(postRepository.findPublishedPosts(any(LocalDateTime.class), eq(pageable)))
                .thenReturn(postPage);

        Page<PostResponse> response = postService.getAllPosts(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(postRepository, times(1))
                .findPublishedPosts(any(LocalDateTime.class), eq(pageable));
    }

    @Test
    void searchPosts_shouldReturnSearchResults() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> postPage = new PageImpl<>(List.of(testPost));
        String query = "test";

        when(postRepository.fullTextSearch(eq(query), eq(pageable)))
                .thenReturn(postPage);

        Page<PostResponse> response = postService.searchPosts(query, pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        verify(postRepository, times(1)).fullTextSearch(eq(query), eq(pageable));
    }

    @Test
    void likePost_shouldToggleLike() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        postService.likePost(1L, testUser);

        verify(postRepository, times(1)).save(any(Post.class));
        verify(userRepository, times(1)).save(any(User.class));
    }
}