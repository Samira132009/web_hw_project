package ru.Edje_7.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts",
        indexes = {
                @Index(name = "idx_posts_slug", columnList = "slug"),
                @Index(name = "idx_posts_status_created", columnList = "status, created_at"),
                @Index(name = "idx_posts_published", columnList = "published_at"),
                @Index(name = "idx_posts_featured", columnList = "is_featured")
        })
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(exclude = {"content", "comments", "tags", "likedBy", "savedBy"})
@NoArgsConstructor
@AllArgsConstructor
public class Post extends AuditEntity {

    public enum Status {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String slug;

    @NotBlank(message = "Content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String excerpt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "is_featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "search_vector", length = 1000)
    private String searchVector;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @BatchSize(size = 20)
    @Fetch(FetchMode.SUBSELECT)
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany(mappedBy = "likedPosts", fetch = FetchType.LAZY)
    private Set<User> likedBy = new HashSet<>();

    @ManyToMany(mappedBy = "savedPosts", fetch = FetchType.LAZY)
    private Set<User> savedBy = new HashSet<>();


    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void publish() {
        if (this.status == Status.DRAFT) {
            this.status = Status.PUBLISHED;
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void archive() {
        this.status = Status.ARCHIVED;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getPosts().add(this);
        tag.setPostCount(tag.getPostCount() + 1);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getPosts().remove(this);
        tag.setPostCount(Math.max(0, tag.getPostCount() - 1));
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(title);
        }

        if ((excerpt == null || excerpt.trim().isEmpty()) && content != null) {
            excerpt = content.length() > 150
                    ? content.substring(0, 150) + "..."
                    : content;
        }

        if (status == Status.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    private String generateSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    public boolean isPublished() {
        return status == Status.PUBLISHED;
    }

    public boolean isAuthor(User user) {
        return author != null && author.getId().equals(user.getId());
    }
}