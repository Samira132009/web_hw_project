package ru.Edje_7.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagResponse {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private Integer postCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private Integer trendingScore;

    private Boolean isTrending;

    private List<TagResponse> relatedTags;

    private Integer weeklyPostCount;

    private Integer monthlyPostCount;
}