package ru.Edje_7.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagService tagService;

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return postRepository.findPublishedPosts(LocalDateTime.now(), pageable)
                .map(this::convertToResponse);
    }

    @Cacheable(value = "post", key = "#id")
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (!post.isPublished()) {
            throw new ResourceNotFoundException("Post is not published");
        }

        post.incrementViewCount();
        postRepository.save(post);

        return convertToResponse(post);
    }

    @Transactional
    public PostResponse createPost(PostRequest request, User author) {
        Post post = new Post();
        post.setAuthor(author);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        
        try {
            String statusStr = request.getStatus() != null ? request.getStatus().toUpperCase() : "PUBLISHED";
            Post.Status status = Post.Status.valueOf(statusStr);
            post.setStatus(status);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status '{}', using PUBLISHED instead", request.getStatus());
            post.setStatus(Post.Status.PUBLISHED);
        }
        
        post.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            request.getTags().forEach(tagName -> {
                tagService.getOrCreateTag(tagName).ifPresent(post::addTag);
            });
        }

        Post savedPost = postRepository.save(post);
        log.info("Created new post with id: {} by user: {} with status: {}", 
                savedPost.getId(), author.getUsername(), savedPost.getStatus());

        return convertToResponse(savedPost);
    }

    @CacheEvict(value = "post", key = "#id")
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (!post.isAuthor(currentUser) && !currentUser.isAdmin()) {
            throw new UnauthorizedException("You are not authorized to update this post");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setFeatured(request.getFeatured());

        if (request.getStatus() != null) {
            post.setStatus(Post.Status.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getTags() != null) {
            post.getTags().forEach(tag -> tag.decrementPostCount());
            post.getTags().clear();

            request.getTags().forEach(tagName -> {
                tagService.getOrCreateTag(tagName).ifPresent(post::addTag);
            });
        }

        Post updatedPost = postRepository.save(post);
        log.info("Updated post with id: {} by user: {}", id, currentUser.getUsername());

        return convertToResponse(updatedPost);
    }

    @CacheEvict(value = "post", key = "#id")
    @Transactional
    public void deletePost(Long id, User currentUser) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        if (!post.isAuthor(currentUser) && !currentUser.isAdmin()) {
            throw new UnauthorizedException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
        log.info("Deleted post with id: {} by user: {}", id, currentUser.getUsername());
    }

    @Cacheable(value = "popularPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public Page<PostResponse> getPopularPosts(Pageable pageable) {
        return postRepository.findPopularPosts(LocalDateTime.now(), pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getAllPosts(pageable);
        }

        return postRepository.fullTextSearch(query.trim(), pageable)
                .map(this::convertToResponse);
    }

    @Transactional
    public void likePost(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.getLikedBy().contains(user)) {
            post.getLikedBy().remove(user);
            user.getLikedPosts().remove(post);
            post.decrementLikeCount();
        } else {
            post.getLikedBy().add(user);
            user.getLikedPosts().add(post);
            post.incrementLikeCount();
        }

        postRepository.save(post);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByTag(String tagName, Pageable pageable) {
        return postRepository.findByTagName(tagName, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorIdAndStatus(authorId, Post.Status.PUBLISHED, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getMyPosts(User currentUser, Pageable pageable) {
        return postRepository.findByAuthorId(currentUser.getId(), pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getRecentPosts(int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return postRepository.findRecentPosts(startDate).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private PostResponse convertToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setSlug(post.getSlug());
        response.setContent(post.getContent());
        response.setExcerpt(post.getExcerpt());
        response.setStatus(post.getStatus().name());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setFeatured(post.getFeatured());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setPublishedAt(post.getPublishedAt());

        User author = post.getAuthor();
        if (author != null) {
            response.setAuthorId(author.getId());
            response.setAuthorUsername(author.getUsername());
            response.setAuthorAvatar(author.getAvatarUrl());
        }

        Set<String> tagNames = post.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toSet());
        response.setTags(tagNames);

        return response;
    }
}