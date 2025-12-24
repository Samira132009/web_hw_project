package ru.Edje_7.service;


import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Edje_7.dto.request.UpdateUserRequest;
import ru.Edje_7.dto.response.UserResponse;
import ru.Edje_7.entity.Role;
import ru.Edje_7.entity.User;
import ru.Edje_7.exceptions.ResourceNotFoundException;
import ru.Edje_7.exceptions.UnauthorizedException;
import ru.Edje_7.repository.PostRepository;
import ru.Edje_7.repository.RoleRepository;
import ru.Edje_7.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.searchUsers(search.trim(), pageable)
                    .map(this::convertToResponse);
        }

        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Cacheable(value = "user", key = "#id")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        return convertToResponse(user);
    }

    @Cacheable(value = "user", key = "#username")
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        return convertToResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        return userRepository.searchUsers(query, pageable)
                .map(this::convertToResponse);
    }

    @CacheEvict(value = "user", key = "#id")
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ValidationException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user with id: {}", id);

        return convertToResponse(updatedUser);
    }

    @CacheEvict(value = "user", key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setEnabled(false);
        userRepository.save(user);

        log.info("Disabled user with id: {}", id);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Changed password for user: {}", user.getUsername());
    }

    @Transactional
    public void followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            throw new ValidationException("Cannot follow yourself");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Follower not found"));

        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Followed user not found"));

        if (!follower.getSubscriptions().contains(followed)) {
            follower.getSubscriptions().add(followed);
            followed.getSubscribers().add(follower);

            userRepository.save(follower);
            userRepository.save(followed);

            log.info("User {} followed user {}", followerId, followedId);
        }
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followedId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Follower not found"));

        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new ResourceNotFoundException("Followed user not found"));

        if (follower.getSubscriptions().contains(followed)) {
            follower.getSubscriptions().remove(followed);
            followed.getSubscribers().remove(follower);

            userRepository.save(follower);
            userRepository.save(followed);

            log.info("User {} unfollowed user {}", followerId, followedId);
        }
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return userRepository.isSubscribed(followerId, followedId);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getFollowers(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userRepository.findFollowers(userId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getFollowing(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userRepository.findFollowing(userId, pageable)
                .map(this::convertToResponse);
    }

    @CacheEvict(value = "user", key = "#userId")
    @Transactional
    public UserResponse updateAvatar(Long userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setAvatarUrl(avatarUrl);
        User updatedUser = userRepository.save(user);

        log.info("Updated avatar for user: {}", user.getUsername());

        return convertToResponse(updatedUser);
    }

    @CacheEvict(value = "user", key = "#userId")
    @Transactional
    public UserResponse updateBio(Long userId, String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setBio(bio);
        User updatedUser = userRepository.save(user);

        log.info("Updated bio for user: {}", user.getUsername());

        return convertToResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Map<String, Object> stats = new java.util.HashMap<>();

        stats.put("userId", user.getId());
        stats.put("username", user.getUsername());
        stats.put("joinedDate", user.getCreatedAt());

        long totalPosts = postRepository.countByAuthorId(userId);
        stats.put("totalPosts", totalPosts);

        stats.put("followersCount", user.getFollowers().size());
        stats.put("followingCount", user.getSubscriptions().size());

        stats.put("totalLikesReceived", 0);
        stats.put("totalCommentsReceived", 0);
        stats.put("totalViews", 0);

        stats.put("lastLogin", user.getLastLoginAt());
        stats.put("accountAgeDays",
                java.time.temporal.ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now()));

        return stats;
    }

    @Transactional
    public boolean verifyEmail(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (token != null && token.equals("verify-" + user.getId())) {
            user.setEmailVerified(true);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    @Transactional
    public void resendVerificationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        log.info("Resending verification email to user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getActiveUsers(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return userRepository.findActiveUsers(pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(String roleName, Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, String roleName, boolean add) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Role role = roleRepository.findByName(Role.RoleName.valueOf(roleName))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

        if (add) {
            if (!user.getRoles().contains(role)) {
                user.addRole(role);
            }
        } else {
            if (user.getRoles().contains(role)) {
                user.removeRole(role);
            }
        }

        User updatedUser = userRepository.save(user);
        log.info("{} role {} for user: {}",
                add ? "Added" : "Removed", roleName, user.getUsername());

        return convertToResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setFullName(user.getFullName());
        response.setBio(user.getBio());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setEmailVerified(user.getEmailVerified());

        long postCount = postRepository.countByAuthorId(user.getId());
        response.setPostCount((int) postCount);

        response.setFollowerCount(user.getFollowers().size());
        response.setFollowingCount(user.getSubscriptions().size());

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
        response.setRoles(roles);

        return response;
    }

    private PostRepository postRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public void setPostRepository(PostRepository postRepository) {
        this.postRepository = postRepository;
    }
}