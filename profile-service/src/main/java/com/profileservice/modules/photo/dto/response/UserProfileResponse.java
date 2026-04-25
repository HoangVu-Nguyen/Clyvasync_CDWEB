package com.profileservice.modules.photo.dto.response;

import com.commoncore.enums.privacy.Privacy;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserProfileResponse {
    private String userId;
    private String username;
    private String bio;
    private String location;
    private LocalDate birthDate;
    private String avatarUrl;
    private String coverUrl;
    private boolean isOwner;
    private Privacy privacy;
    private boolean canViewPrivateInfo;
}