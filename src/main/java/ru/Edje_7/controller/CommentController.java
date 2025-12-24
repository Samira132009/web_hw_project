package ru.Edje_7.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.Edje_7.dto.PaginationResponse;
import ru.Edje_7.dto.request.CommentRequest;
import ru.Edje_7.dto.response.ApiResponse;
import ru.Edje_7.dto.response.CommentResponse;
import ru.Edje_7.entity.User;
import ru.Edje_7.service.AuthService;
import ru.Edje_7.service.CommentService;

import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management endpoints")
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    @Operation(summary = "Get comments for a post")
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResponse<PaginationResponse<CommentResponse>>> getCommentsByPost(
            @PathVariable Long postId,
            Pageable pageable) {

        Page<CommentResponse> comments = commentService.getCommentsByPostId(postId, pageable);
        PaginationResponse<CommentResponse> response = PaginationResponse.fromPage(comments);

        return ResponseEntity.ok(ApiResponse.success(response, "Comments retrieved successfully"));
    }

    @Operation(summary = "Get comment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable Long id) {
        CommentResponse comment = commentService.getCommentById(id);
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment retrieved successfully"));
    }

    @Operation(summary = "Get replies for a comment")
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<ApiResponse<PaginationResponse<CommentResponse>>> getCommentReplies(
            @PathVariable Long commentId,
            Pageable pageable) {

        Page<CommentResponse> replies = commentService.getCommentReplies(commentId, pageable);
        PaginationResponse<CommentResponse> response = PaginationResponse.fromPage(replies);

        return ResponseEntity.ok(ApiResponse.success(response, "Comment replies retrieved successfully"));
    }

    @Operation(
            summary = "Create new comment",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CommentRequest request) {

        User currentUser = authService.getCurrentUser();
        CommentResponse comment = commentService.createComment(request, currentUser);

        return ResponseEntity.ok(ApiResponse.success(comment, "Comment created successfully"));
    }

    @Operation(
            summary = "Create reply to comment",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{parentId}/reply")
    public ResponseEntity<ApiResponse<CommentResponse>> createReply(
            @PathVariable Long parentId,
            @Valid @RequestBody CommentRequest request) {

        User currentUser = authService.getCurrentUser();
        CommentResponse reply = commentService.createReply(parentId, request, currentUser);

        return ResponseEntity.ok(ApiResponse.success(reply, "Reply created successfully"));
    }

    @Operation(
            summary = "Update comment",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request) {

        User currentUser = authService.getCurrentUser();
        CommentResponse comment = commentService.updateComment(id, request, currentUser);

        return ResponseEntity.ok(ApiResponse.success(comment, "Comment updated successfully"));
    }

    @Operation(
            summary = "Delete comment",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        commentService.deleteComment(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }

    @Operation(
            summary = "Like/unlike comment",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> likeComment(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        boolean liked = commentService.likeComment(id, currentUser);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("liked", liked),
                liked ? "Comment liked successfully" : "Comment unliked successfully"
        ));
    }

    @Operation(summary = "Get user's comments")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PaginationResponse<CommentResponse>>> getUserComments(
            @PathVariable Long userId,
            Pageable pageable) {

        Page<CommentResponse> comments = commentService.getCommentsByUserId(userId, pageable);
        PaginationResponse<CommentResponse> response = PaginationResponse.fromPage(comments);

        return ResponseEntity.ok(ApiResponse.success(response, "User comments retrieved successfully"));
    }

    @Operation(summary = "Get recent comments")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<PaginationResponse<CommentResponse>>> getRecentComments(
            @RequestParam(defaultValue = "7") int days,
            Pageable pageable) {

        Page<CommentResponse> comments = commentService.getRecentComments(days, pageable);
        PaginationResponse<CommentResponse> response = PaginationResponse.fromPage(comments);

        return ResponseEntity.ok(ApiResponse.success(response, "Recent comments retrieved successfully"));
    }
}