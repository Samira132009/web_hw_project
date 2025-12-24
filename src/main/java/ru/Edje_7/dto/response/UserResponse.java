package ru.Edje_7.dto.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private Long id;

    private String email;

    private String username;

    private String firstName;

    private String lastName;

    private String bio;

    private String avatarUrl;

    private String fullName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginAt;

    private Boolean emailVerified;

    private Integer postCount;

    private Integer followerCount;

    private Integer followingCount;

    private Set<String> roles;

    private Boolean isFollowing;


}
