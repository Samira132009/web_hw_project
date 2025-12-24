package ru.Edje_7.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponse {

    private Long id;

    private String title;

    private String slug;

    private String content;

    private String excerpt;

    private String status;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    private Boolean featured;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedAt;

    private Long authorId;

    private String authorUsername;

    private String authorAvatar;

    private String authorBio;

    private Set<String> tags;

    private Boolean liked;

    private Boolean saved;

    private Boolean followingAuthor;

    private Integer authorPostCount;

    private Integer authorFollowerCount;

    private String metaTitle;

    private String metaDescription;

    private String canonicalUrl;

    private Integer readTime;

    private PostNavigation navigation;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PostNavigation {
        private Long previousPostId;
        private String previousPostTitle;
        private String previousPostSlug;

        private Long nextPostId;
        private String nextPostTitle;
        private String nextPostSlug;
    }
}
