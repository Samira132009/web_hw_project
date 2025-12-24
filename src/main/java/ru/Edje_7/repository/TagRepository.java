package ru.Edje_7.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.Edje_7.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findBySlug(String slug);

    boolean existsByName(String name);

    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Tag> searchTags(@Param("query") String query, Pageable pageable);

    @Query("SELECT t FROM Tag t JOIN t.posts p WHERE p.id = :postId")
    List<Tag> findByPostId(@Param("postId") Long postId);

    @Query("SELECT t FROM Tag t ORDER BY SIZE(t.posts) DESC")
    Page<Tag> findPopularTags(Pageable pageable);

    @Query(value = "SELECT t.* FROM tags t " +
            "JOIN post_tags pt ON t.id = pt.tag_id " +
            "JOIN posts p ON pt.post_id = p.id " +
            "WHERE p.created_at >= :since " +
            "GROUP BY t.id " +
            "ORDER BY COUNT(p.id) DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<Tag> findTrendingTagsWithLimit(@Param("since") LocalDateTime since,
                                        @Param("limit") int limit);
}