package com.profileservice.modules.photo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private Long userId;
    private String bio;
    private String username;
    private String location;
    private LocalDate birthDate;
    private String avatarUrl;
    private String coverUrl;
}