package ru.Edje_7.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.Edje_7.dto.PaginationResponse;
import ru.Edje_7.dto.request.PostRequest;
import ru.Edje_7.dto.response.ApiResponse;
import ru.Edje_7.dto.response.PostResponse;
import ru.Edje_7.entity.User;
import ru.Edje_7.exceptions.UnauthorizedException;
import ru.Edje_7.service.AuthService;
import ru.Edje_7.service.PostService;

import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Post management endpoints")
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    @Operation(summary = "Get all published posts")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponse>>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "publishedAt,desc") String[] sort) {

        Sort.Direction direction = sort.length > 1 && "desc".equalsIgnoreCase(sort[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

        Page<PostResponse> posts = postService.getAllPosts(pageable);
        PaginationResponse<PostResponse> response = PaginationResponse.fromPage(posts);

        return ResponseEntity.ok(ApiResponse.success(response, "Posts retrieved successfully"));
    }

    @Operation(summary = "Get post by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        return ResponseEntity.ok(ApiResponse.success(post, "Post retrieved successfully"));
    }

    @Operation(summary = "Search posts")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponse>>> searchPosts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.searchPosts(q, pageable);
        PaginationResponse<PostResponse> response = PaginationResponse.fromPage(posts);

        return ResponseEntity.ok(ApiResponse.success(response, "Search results"));
    }

    @Operation(summary = "Get popular posts")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponse>>> getPopularPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getPopularPosts(pageable);
        PaginationResponse<PostResponse> response = PaginationResponse.fromPage(posts);

        return ResponseEntity.ok(ApiResponse.success(response, "Popular posts"));
    }

    @Operation(
            summary = "Create new post",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostRequest request) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required. Please provide a valid JWT token in the Authorization header.");
        }
        
        PostResponse post = postService.createPost(request, currentUser);

        return ResponseEntity.ok(ApiResponse.success(post, "Post created successfully"));
    }

    @Operation(
            summary = "Update post",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {

        User currentUser = authService.getCurrentUser();
        PostResponse post = postService.updatePost(id, request, currentUser);

        return ResponseEntity.ok(ApiResponse.success(post, "Post updated successfully"));
    }

    @Operation(
            summary = "Delete post",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        postService.deletePost(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(null, "Post deleted successfully"));
    }

    @Operation(
            summary = "Like/unlike post",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> likePost(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        postService.likePost(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("liked", true),
                "Post liked successfully"
        ));
    }

    @Operation(summary = "Get posts by tag")
    @GetMapping("/tag/{tagName}")
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponse>>> getPostsByTag(
            @PathVariable String tagName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getPostsByTag(tagName, pageable);
        PaginationResponse<PostResponse> response = PaginationResponse.fromPage(posts);

        return ResponseEntity.ok(ApiResponse.success(response, "Posts by tag"));
    }

    @Operation(summary = "Get posts by author")
    @GetMapping("/author/{authorId}")
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponse>>> getPostsByAuthor(
            @PathVariable Long authorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getPostsByAuthor(authorId, pageable);
        PaginationResponse<PostResponse> response = PaginationResponse.fromPage(posts);

        return ResponseEntity.ok(ApiResponse.success(response, "Posts by author"));
    }

    @Operation(
            summary = "Get my posts (including drafts)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PaginationResponse<PostResponse>>> getMyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getMyPosts(currentUser, pageable);
        PaginationResponse<PostResponse> response = PaginationResponse.fromPage(posts);

        return ResponseEntity.ok(ApiResponse.success(response, "My posts"));
    }
}