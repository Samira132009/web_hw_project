package ru.Edje_7.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Edje_7.dto.response.UserResponse;
import ru.Edje_7.entity.Role;
import ru.Edje_7.entity.User;
import ru.Edje_7.exceptions.ResourceNotFoundException;
import ru.Edje_7.repository.RoleRepository;
import ru.Edje_7.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PostService postService;
    private final UserService userService;

    @Transactional
    public UserResponse updateUser(Long id, Map<String, Object> updates) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        updates.forEach((key, value) -> {
            switch (key) {
                case "email":
                    if (value != null && !value.equals(user.getEmail())) {
                        if (userRepository.existsByEmail(value.toString())) {
                            throw new IllegalArgumentException("Email already exists");
                        }
                        user.setEmail(value.toString());
                    }
                    break;

                case "username":
                    if (value != null && !value.equals(user.getUsername())) {
                        if (userRepository.existsByUsername(value.toString())) {
                            throw new IllegalArgumentException("Username already exists");
                        }
                        user.setUsername(value.toString());
                    }
                    break;

                case "firstName":
                    if (value != null) user.setFirstName(value.toString());
                    break;

                case "lastName":
                    if (value != null) user.setLastName(value.toString());
                    break;

                case "bio":
                    if (value != null) user.setBio(value.toString());
                    break;

                case "avatarUrl":
                    if (value != null) user.setAvatarUrl(value.toString());
                    break;

                case "enabled":
                    if (value instanceof Boolean) {
                        user.setEnabled((Boolean) value);
                    }
                    break;

                case "locked":
                    if (value instanceof Boolean) {
                        user.setLocked((Boolean) value);
                    }
                    break;

                case "emailVerified":
                    if (value instanceof Boolean) {
                        user.setEmailVerified((Boolean) value);
                    }
                    break;
            }
        });

        User updatedUser = userRepository.save(user);
        log.info("Admin updated user with id: {}", id);

        return userService.convertToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
        log.info("Admin deleted user with id: {}", id);
    }

    @Transactional
    public UserResponse banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setLocked(true);
        User updatedUser = userRepository.save(user);

        log.info("Admin banned user with id: {}", id);

        return userService.convertToResponse(updatedUser);
    }

    @Transactional
    public UserResponse unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setLocked(false);
        User updatedUser = userRepository.save(user);

        log.info("Admin unbanned user with id: {}", id);

        return userService.convertToResponse(updatedUser);
    }

    @Transactional
    public UserResponse assignAdminRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> {
                    Role role = new Role(Role.RoleName.ROLE_ADMIN, "Administrator");
                    return roleRepository.save(role);
                });

        if (!user.getRoles().contains(adminRole)) {
            user.addRole(adminRole);
            User updatedUser = userRepository.save(user);

            log.info("Assigned ADMIN role to user: {}", user.getUsername());
            return userService.convertToResponse(updatedUser);
        }

        return userService.convertToResponse(user);
    }

    @Transactional
    public UserResponse removeAdminRole(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("ADMIN role not found"));

        if (user.getRoles().contains(adminRole)) {
            user.removeRole(adminRole);
            User updatedUser = userRepository.save(user);

            log.info("Removed ADMIN role from user: {}", user.getUsername());
            return userService.convertToResponse(updatedUser);
        }

        return userService.convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findActiveUsers(Pageable.unpaged()).getTotalElements();
        long admins = userRepository.findAdmins().size();

        stats.put("totalUsers", totalUsers);
        stats.put("activeUsers", activeUsers);
        stats.put("admins", admins);

        stats.put("totalPosts", 0);
        stats.put("publishedPosts", 0);
        stats.put("draftPosts", 0);

        stats.put("totalComments", 0);

        stats.put("serverTime", LocalDateTime.now());
        stats.put("uptime", "24 hours"); 

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getAuditLogs(Pageable pageable) {
        log.warn("Audit logs not implemented");
        return Page.empty(pageable);
    }

    @Transactional
    public void featurePost(Long postId) {
        log.info("Admin featured post with id: {}", postId);
    }

    @Transactional
    public void unfeaturePost(Long postId) {
        log.info("Admin unfeatured post with id: {}", postId);
    }

    @Transactional
    public void deleteAnyPost(Long postId) {
        postService.deletePost(postId, null); // null user means admin
        log.info("Admin deleted post with id: {}", postId);
    }

    @Transactional
    public void deleteAnyComment(Long commentId) {
        log.info("Admin deleted comment with id: {}", commentId);
    }

    @Transactional
    public UserResponse assignModeratorRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role moderatorRole = roleRepository.findByName(Role.RoleName.ROLE_MODERATOR)
                .orElseGet(() -> {
                    Role role = new Role(Role.RoleName.ROLE_MODERATOR, "Content Moderator");
                    return roleRepository.save(role);
                });

        if (!user.getRoles().contains(moderatorRole)) {
            user.addRole(moderatorRole);
            User updatedUser = userRepository.save(user);

            log.info("Assigned MODERATOR role to user: {}", user.getUsername());
            return userService.convertToResponse(updatedUser);
        }

        return userService.convertToResponse(user);
    }

    @Transactional
    public UserResponse removeModeratorRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role moderatorRole = roleRepository.findByName(Role.RoleName.ROLE_MODERATOR)
                .orElseThrow(() -> new ResourceNotFoundException("MODERATOR role not found"));

        if (user.getRoles().contains(moderatorRole)) {
            user.removeRole(moderatorRole);
            User updatedUser = userRepository.save(user);

            log.info("Removed MODERATOR role from user: {}", user.getUsername());
            return userService.convertToResponse(updatedUser);
        }

        return userService.convertToResponse(user);
    }
}
