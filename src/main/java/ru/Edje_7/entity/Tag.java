package ru.Edje_7.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "posts")
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Tag name is required")
    @Size(min = 1, max = 50, message = "Tag name must be between 1 and 50 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Post> posts = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(name);
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    public void incrementPostCount() {
        this.postCount++;
    }

    public void decrementPostCount() {
        if (this.postCount > 0) {
            this.postCount--;
        }
    }
}
