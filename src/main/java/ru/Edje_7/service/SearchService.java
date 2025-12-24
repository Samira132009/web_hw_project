package ru.Edje_7.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Edje_7.dto.PaginationResponse;
import ru.Edje_7.dto.response.PostResponse;
import ru.Edje_7.dto.response.UserResponse;
import ru.Edje_7.entity.Post;
import ru.Edje_7.entity.User;
import ru.Edje_7.repository.PostRepository;
import ru.Edje_7.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Map<String, Object> globalSearch(String query, Pageable pageable) {
        Map<String, Object> results = new HashMap<>();

        Page<PostResponse> posts = searchPosts(query, pageable);
        results.put("posts", PaginationResponse.fromPage(posts));

        Page<UserResponse> users = searchUsers(query, pageable);
        results.put("users", PaginationResponse.fromPage(users));

        results.put("query", query);
        results.put("totalResults", posts.getTotalElements() + users.getTotalElements());

        return results;
    }

    @Cacheable(value = "searchPosts", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty() || query.trim().length() < 2) {
            return Page.empty(pageable);
        }

        String searchQuery = query.trim().toLowerCase();

        log.debug("Searching posts with query: {}", searchQuery);

        Page<Post> posts = postRepository.fullTextSearch(searchQuery, pageable);

        return posts.map(post -> {
            PostResponse response = new PostResponse();
            response.setId(post.getId());
            response.setTitle(post.getTitle());
            response.setSlug(post.getSlug());
            response.setExcerpt(post.getExcerpt());
            response.setViewCount(post.getViewCount());
            response.setLikeCount(post.getLikeCount());
            response.setCommentCount(post.getCommentCount());
            response.setCreatedAt(post.getCreatedAt());
            response.setPublishedAt(post.getPublishedAt());

            if (post.getAuthor() != null) {
                response.setAuthorId(post.getAuthor().getId());
                response.setAuthorUsername(post.getAuthor().getUsername());
                response.setAuthorAvatar(post.getAuthor().getAvatarUrl());
            }

            return response;
        });
    }

    @Cacheable(value = "searchUsers", key = "#query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty() || query.trim().length() < 2) {
            return Page.empty(pageable);
        }

        String searchQuery = query.trim();

        log.debug("Searching users with query: {}", searchQuery);

        Page<User> users = userRepository.searchUsers(searchQuery, pageable);

        return users.map(user -> {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setFullName(user.getFullName());
            response.setAvatarUrl(user.getAvatarUrl());
            response.setBio(user.getBio());
            response.setCreatedAt(user.getCreatedAt());
            response.setLastLoginAt(user.getLastLoginAt());

            long postCount = postRepository.countByAuthorId(user.getId());
            response.setPostCount((int) postCount);

            return response;
        });
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> advancedPostSearch(
            String query,
            Long authorId,
            Long tagId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Boolean featured,
            Integer minViews,
            Integer minLikes,
            Pageable pageable) {

        
        if (query != null && !query.trim().isEmpty()) {
            return searchPosts(query, pageable);
        }

        
        if (authorId != null) {
            return postService.getPostsByAuthor(authorId, pageable);
        }

       
        return postRepository.findPublishedPosts(LocalDateTime.now(), pageable)
                .map(post -> {
                    PostResponse response = new PostResponse();
                    response.setId(post.getId());
                    response.setTitle(post.getTitle());
                    response.setSlug(post.getSlug());
                    response.setExcerpt(post.getExcerpt());
                    
                    return response;
                });
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSearchStatistics(String query) {
        Map<String, Long> stats = new HashMap<>();

        if (query == null || query.trim().isEmpty()) {
            return stats;
        }

        String searchQuery = query.trim();

        Page<Post> posts = postRepository.fullTextSearch(searchQuery, Pageable.unpaged());
        stats.put("posts", posts.getTotalElements());

        Page<User> users = userRepository.searchUsers(searchQuery, Pageable.unpaged());
        stats.put("users", users.getTotalElements());

        stats.put("tags", 0L);

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> searchByTags(String[] tags, Pageable pageable) {
        log.warn("Search by multiple tags not yet implemented");
        return Page.empty(pageable);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> searchSimilarPosts(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        String tagsQuery = String.join(" ", post.getTags().stream()
                .map(tag -> tag.getName())
                .toArray(String[]::new));

        if (!tagsQuery.isEmpty()) {
            return searchPosts(tagsQuery, pageable);
        }

        String contentSnippet = post.getContent().length() > 100
                ? post.getContent().substring(0, 100)
                : post.getContent();

        return searchPosts(contentSnippet, pageable);
    }
}