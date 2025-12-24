package ru.Edje_7.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.Edje_7.dto.PaginationResponse;
import ru.Edje_7.dto.response.ApiResponse;
import ru.Edje_7.dto.response.UserResponse;
import ru.Edje_7.service.AdminService;
import ru.Edje_7.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrator management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @Operation(summary = "Get all users (admin only)")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            Pageable pageable) {

        Page<UserResponse> users = userService.getAllUsers(search, pageable);
        PaginationResponse<UserResponse> response = PaginationResponse.fromPage(users);

        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved successfully"));
    }

    @Operation(summary = "Get user by ID (admin only)")
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @Operation(summary = "Update user (admin only)")
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        UserResponse updatedUser = adminService.updateUser(id, updates);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @Operation(summary = "Delete user (admin only)")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @Operation(summary = "Ban user (admin only)")
    @PostMapping("/users/{id}/ban")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(@PathVariable Long id) {
        UserResponse user = adminService.banUser(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User banned successfully"));
    }

    @Operation(summary = "Unban user (admin only)")
    @PostMapping("/users/{id}/unban")
    public ResponseEntity<ApiResponse<UserResponse>> unbanUser(@PathVariable Long id) {
        UserResponse user = adminService.unbanUser(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User unbanned successfully"));
    }

    @Operation(summary = "Assign admin role to user")
    @PostMapping("/users/{id}/assign-admin")
    public ResponseEntity<ApiResponse<UserResponse>> assignAdminRole(@PathVariable Long id) {
        UserResponse user = adminService.assignAdminRole(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Admin role assigned successfully"));
    }

    @Operation(summary = "Remove admin role from user")
    @PostMapping("/users/{id}/remove-admin")
    public ResponseEntity<ApiResponse<UserResponse>> removeAdminRole(@PathVariable Long id) {
        UserResponse user = adminService.removeAdminRole(id);
        return ResponseEntity.ok(ApiResponse.success(user, "Admin role removed successfully"));
    }

    @Operation(summary = "Get system statistics")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        Map<String, Object> statistics = adminService.getSystemStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
    }

    @Operation(summary = "Get audit logs")
    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<PaginationResponse<Map<String, Object>>>> getAuditLogs(
            Pageable pageable) {

        Page<Map<String, Object>> logs = adminService.getAuditLogs(pageable);
        PaginationResponse<Map<String, Object>> response = PaginationResponse.fromPage(logs);

        return ResponseEntity.ok(ApiResponse.success(response, "Audit logs retrieved successfully"));
    }

    @Operation(summary = "Feature a post")
    @PostMapping("/posts/{postId}/feature")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> featurePost(@PathVariable Long postId) {
        adminService.featurePost(postId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("featured", true),
                "Post featured successfully"
        ));
    }

    @Operation(summary = "Unfeature a post")
    @PostMapping("/posts/{postId}/unfeature")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> unfeaturePost(@PathVariable Long postId) {
        adminService.unfeaturePost(postId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("featured", false),
                "Post unfeatured successfully"
        ));
    }

    @Operation(summary = "Delete any post (admin only)")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnyPost(@PathVariable Long postId) {
        adminService.deleteAnyPost(postId);
        return ResponseEntity.ok(ApiResponse.success(null, "Post deleted successfully"));
    }

    @Operation(summary = "Delete any comment (admin only)")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnyComment(@PathVariable Long commentId) {
        adminService.deleteAnyComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Comment deleted successfully"));
    }
}
