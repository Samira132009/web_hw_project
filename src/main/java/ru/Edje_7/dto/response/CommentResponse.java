package ru.Edje_7.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.Edje_7.dto.PaginationResponse;


import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponse {

    private Long id;

    private String content;

    private Boolean isDeleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long userId;

    private String username;

    private String userAvatar;

    private String userBio;

    private Long postId;

    private String postTitle;

    private String postSlug;

    private Long parentId;

    private String parentUsername;

    private Integer likeCount;

    private Integer replyCount;

    private Boolean liked;

    private Boolean canEdit;

    private Boolean canDelete;

    private List<CommentResponse> replies;

    private PaginationResponse<CommentResponse> repliesPage;
}