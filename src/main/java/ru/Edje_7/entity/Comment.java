package ru.Edje_7.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "comments")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(exclude = {"post", "user", "replies"})
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> replies = new HashSet<>();

    @NotBlank(message = "Comment content is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;


    public void addReply(Comment reply) {
        replies.add(reply);
        reply.setParent(this);
        post.incrementCommentCount();
    }

    public void delete() {
        this.isDeleted = true;
        replies.forEach(Comment::delete);
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isAuthor(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }
}
