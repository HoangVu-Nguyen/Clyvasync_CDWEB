package com.profileservice.modules.profile.dto.response;

import com.commoncore.enums.privacy.Privacy;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
    private String website;
    private String relationshipStatus;
    private boolean isOwner;
    private Privacy privacy;
    private boolean canViewPrivateInfo;
    private List<UserWorkplaceResponse> workplaces;
    private List<UserEducationResponse> educations;
}