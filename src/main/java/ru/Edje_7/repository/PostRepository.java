package ru.Edje_7.repository;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.Edje_7.entity.Post;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Cacheable(value = "posts", key = "#slug")
    Optional<Post> findBySlug(String slug);

    @Cacheable(value = "postsByAuthor", key = "#authorId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);

    Page<Post> findByAuthorIdAndStatus(Long authorId, Post.Status status, Pageable pageable);

    Page<Post> findByStatus(Post.Status status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND " +
            "(p.publishedAt IS NULL OR p.publishedAt <= :now) " +
            "ORDER BY p.publishedAt DESC NULLS LAST")
    Page<Post> findPublishedPosts(@Param("now") LocalDateTime now, Pageable pageable);

    @Cacheable(value = "popularPosts", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND " +
            "(p.publishedAt IS NULL OR p.publishedAt <= :now) " +
            "ORDER BY p.viewCount DESC, p.likeCount DESC, p.publishedAt DESC")
    Page<Post> findPopularPosts(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.id = :tagId AND p.status = 'PUBLISHED'")
    Page<Post> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tagName AND p.status = 'PUBLISHED'")
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.featured = true AND p.status = 'PUBLISHED'")
    Page<Post> findFeaturedPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.excerpt) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Post> searchByKeyword(@Param("query") String query, Pageable pageable);

    @Query(value = """
        SELECT p.* FROM posts p 
        WHERE p.status = 'PUBLISHED' 
        AND (p.search_vector @@ plainto_tsquery('english', :query)
             OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')))
        ORDER BY ts_rank(p.search_vector, plainto_tsquery('english', :query)) DESC, 
                 p.published_at DESC NULLS LAST
        """,
            countQuery = """
        SELECT COUNT(*) FROM posts p 
        WHERE p.status = 'PUBLISHED' 
        AND (p.search_vector @@ plainto_tsquery('english', :query)
             OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%')))
        """,
            nativeQuery = true)
    Page<Post> fullTextSearch(@Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(p) > 0 FROM Post p JOIN p.likedBy u WHERE p.id = :postId AND u.id = :userId")
    boolean isLikedByUser(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT COUNT(p) > 0 FROM Post p JOIN p.savedBy u WHERE p.id = :postId AND u.id = :userId")
    boolean isSavedByUser(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT p FROM Post p JOIN p.author.subscribers s WHERE s.id = :userId AND p.status = 'PUBLISHED'")
    Page<Post> findPostsFromSubscriptions(@Param("userId") Long userId, Pageable pageable);

    Long countByAuthorId(Long authorId);

    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.createdAt >= :startDate")
    List<Post> findRecentPosts(@Param("startDate") LocalDateTime startDate);
}