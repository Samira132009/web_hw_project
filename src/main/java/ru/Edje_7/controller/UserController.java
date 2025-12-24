package ru.Edje_7.controller;


import io.swagger.v3.oas.annotations.Operation;
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
import ru.Edje_7.dto.request.UpdateUserRequest;
import ru.Edje_7.dto.response.ApiResponse;
import ru.Edje_7.dto.response.UserResponse;
import ru.Edje_7.entity.User;
import ru.Edje_7.service.AuthService;
import ru.Edje_7.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        User currentUser = authService.getCurrentUser();
        UserResponse user = userService.getUserById(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user, "User profile retrieved successfully"));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @Valid @RequestBody UpdateUserRequest request) {

        User currentUser = authService.getCurrentUser();
        UserResponse updatedUser = userService.updateUser(currentUser.getId(), request);

        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Profile updated successfully"));
    }

    @Operation(summary = "Delete current user account")
    @DeleteMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUser() {
        User currentUser = authService.getCurrentUser();
        userService.deleteUser(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }

    @Operation(summary = "Change password")
    @PostMapping("/me/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword) {

        User currentUser = authService.getCurrentUser();
        userService.changePassword(currentUser.getId(), currentPassword, newPassword);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("changed", true),
                "Password changed successfully"
        ));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @Operation(summary = "Get user by username")
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @Operation(summary = "Search users")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> searchUsers(
            @RequestParam String q,
            Pageable pageable) {

        Page<UserResponse> users = userService.searchUsers(q, pageable);
        PaginationResponse<UserResponse> response = PaginationResponse.fromPage(users);

        return ResponseEntity.ok(ApiResponse.success(response, "Users search results"));
    }

    @Operation(summary = "Get user's followers")
    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getFollowers(
            @PathVariable Long userId,
            Pageable pageable) {

        Page<UserResponse> followers = userService.getFollowers(userId, pageable);
        PaginationResponse<UserResponse> response = PaginationResponse.fromPage(followers);

        return ResponseEntity.ok(ApiResponse.success(response, "Followers retrieved successfully"));
    }

    @Operation(summary = "Get user's following")
    @GetMapping("/{userId}/following")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getFollowing(
            @PathVariable Long userId,
            Pageable pageable) {

        Page<UserResponse> following = userService.getFollowing(userId, pageable);
        PaginationResponse<UserResponse> response = PaginationResponse.fromPage(following);

        return ResponseEntity.ok(ApiResponse.success(response, "Following retrieved successfully"));
    }

    @Operation(
            summary = "Follow user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> followUser(@PathVariable Long userId) {
        User currentUser = authService.getCurrentUser();
        userService.followUser(currentUser.getId(), userId);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("following", true),
                "User followed successfully"
        ));
    }

    @Operation(
            summary = "Unfollow user",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{userId}/unfollow")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> unfollowUser(@PathVariable Long userId) {
        User currentUser = authService.getCurrentUser();
        userService.unfollowUser(currentUser.getId(), userId);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("following", false),
                "User unfollowed successfully"
        ));
    }

    @Operation(summary = "Check if following user")
    @GetMapping("/{userId}/is-following")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> isFollowing(@PathVariable Long userId) {
        User currentUser = authService.getCurrentUser();
        boolean isFollowing = userService.isFollowing(currentUser.getId(), userId);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("following", isFollowing),
                isFollowing ? "You are following this user" : "You are not following this user"
        ));
    }

    @Operation(
            summary = "Upload profile picture",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserResponse>> uploadAvatar(
            @RequestParam String avatarUrl) {

        User currentUser = authService.getCurrentUser();
        UserResponse user = userService.updateAvatar(currentUser.getId(), avatarUrl);

        return ResponseEntity.ok(ApiResponse.success(user, "Avatar updated successfully"));
    }

    @Operation(
            summary = "Update user bio",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/bio")
    public ResponseEntity<ApiResponse<UserResponse>> updateBio(
            @RequestParam String bio) {

        User currentUser = authService.getCurrentUser();
        UserResponse user = userService.updateBio(currentUser.getId(), bio);

        return ResponseEntity.ok(ApiResponse.success(user, "Bio updated successfully"));
    }

    @Operation(summary = "Get user statistics")
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics(@PathVariable Long userId) {
        Map<String, Object> statistics = userService.getUserStatistics(userId);
        return ResponseEntity.ok(ApiResponse.success(statistics, "User statistics retrieved successfully"));
    }

    @Operation(
            summary = "Verify email",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/verify-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyEmail(
            @RequestParam String token) {

        User currentUser = authService.getCurrentUser();
        boolean verified = userService.verifyEmail(currentUser.getId(), token);

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("verified", verified),
                verified ? "Email verified successfully" : "Email verification failed"
        ));
    }

    @Operation(
            summary = "Resend verification email",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/resend-verification")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> resendVerification() {
        User currentUser = authService.getCurrentUser();
        userService.resendVerificationEmail(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(
                Map.of("sent", true),
                "Verification email sent successfully"
        ));
    }

    @Operation(summary = "Get active users")
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getActiveUsers(
            @RequestParam(defaultValue = "30") int days,
            Pageable pageable) {

        Page<UserResponse> users = userService.getActiveUsers(days, pageable);
        PaginationResponse<UserResponse> response = PaginationResponse.fromPage(users);

        return ResponseEntity.ok(ApiResponse.success(response, "Active users retrieved successfully"));
    }
}