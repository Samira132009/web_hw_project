package ru.Edje_7.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.Edje_7.dto.request.CommentRequest;
import ru.Edje_7.dto.response.CommentResponse;
import ru.Edje_7.entity.Comment;
import ru.Edje_7.entity.Post;
import ru.Edje_7.entity.User;
import ru.Edje_7.exceptions.ResourceNotFoundException;
import ru.Edje_7.exceptions.UnauthorizedException;
import ru.Edje_7.repository.CommentRepository;
import ru.Edje_7.repository.PostRepository;
import ru.Edje_7.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        return commentRepository.findByPostIdAndParentIsNull(postId, pageable)
                .map(comment -> convertToResponse(comment, null));
    }

    @Cacheable(value = "comment", key = "#id")
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        return convertToResponse(comment, null);
    }

    @Transactional
    public CommentResponse createComment(CommentRequest request, User user) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + request.getPostId()));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.getContent());

        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParent(parent);
        }

        Comment savedComment = commentRepository.save(comment);

        post.incrementCommentCount();
        postRepository.save(post);

        log.info("Created comment with id: {} by user: {}", savedComment.getId(), user.getUsername());

        return convertToResponse(savedComment, user);
    }

    @Transactional
    public CommentResponse createReply(Long parentId, CommentRequest request, User user) {
        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));

        Comment reply = new Comment();
        reply.setPost(parent.getPost());
        reply.setUser(user);
        reply.setParent(parent);
        reply.setContent(request.getContent());

        Comment savedReply = commentRepository.save(reply);

        Post post = parent.getPost();
        post.incrementCommentCount();
        postRepository.save(post);

        log.info("Created reply with id: {} to comment: {} by user: {}",
                savedReply.getId(), parentId, user.getUsername());

        return convertToResponse(savedReply, user);
    }

    @CacheEvict(value = "comment", key = "#id")
    @Transactional
    public CommentResponse updateComment(Long id, CommentRequest request, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        if (!comment.isAuthor(currentUser) && !currentUser.isAdmin() && !currentUser.isModerator()) {
            throw new UnauthorizedException("You are not authorized to update this comment");
        }

        comment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Updated comment with id: {} by user: {}", id, currentUser.getUsername());

        return convertToResponse(updatedComment, currentUser);
    }

    @CacheEvict(value = "comment", key = "#id")
    @Transactional
    public void deleteComment(Long id, User currentUser) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        if (!comment.isAuthor(currentUser) && !currentUser.isAdmin() && !currentUser.isModerator()) {
            throw new UnauthorizedException("You are not authorized to delete this comment");
        }

        comment.delete();
        commentRepository.save(comment);

        Post post = comment.getPost();
        post.decrementCommentCount();
        postRepository.save(post);

        log.info("Deleted comment with id: {} by user: {}", id, currentUser.getUsername());
    }

    @Transactional
    public boolean likeComment(Long commentId, User user) {
        return true;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByUserId(Long userId, Pageable pageable) {
        return commentRepository.findByUserId(userId, pageable)
                .map(comment -> convertToResponse(comment, null));
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentReplies(Long commentId, Pageable pageable) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Page<Comment> repliesPage = (Page<Comment>) commentRepository.findByParentId(commentId);

        return repliesPage.map(comment -> convertToResponse(comment, null));
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getRecentComments(int days, Pageable pageable) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return commentRepository.findRecentComments(since, pageable)
                .map(comment -> convertToResponse(comment, null));
    }

    private CommentResponse convertToResponse(Comment comment, User currentUser) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setIsDeleted(comment.getParent().isDeleted());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getUser() != null) {
            response.setUserId(comment.getUser().getId());
            response.setUsername(comment.getUser().getUsername());
            response.setUserAvatar(comment.getUser().getAvatarUrl());
            response.setUserBio(comment.getUser().getBio());
        }

        if (comment.getPost() != null) {
            response.setPostId(comment.getPost().getId());
            response.setPostTitle(comment.getPost().getTitle());
            response.setPostSlug(comment.getPost().getSlug());
        }

        if (comment.getParent() != null) {
            response.setParentId(comment.getParent().getId());
            if (comment.getParent().getUser() != null) {
                response.setParentUsername(comment.getParent().getUser().getUsername());
            }
        }

        response.setReplyCount(comment.getReplies().size());

        if (currentUser != null) {
            response.setCanEdit(comment.isAuthor(currentUser) || currentUser.isAdmin());
            response.setCanDelete(comment.isAuthor(currentUser) || currentUser.isAdmin() || currentUser.isModerator());
        }

        return response;
    }
}
