package ru.Edje_7.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.Edje_7.entity.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdAndParentIsNull(Long postId, Pageable pageable);

    List<Comment> findByParentId(Long parentId);

    Page<Comment> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isDeleted = false")
    int countByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c WHERE c.createdAt >= :since AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<Comment> findRecentComments(@Param("since") LocalDateTime since, Pageable pageable);
}