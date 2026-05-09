package com.profileservice.modules.profile.dto.request;

import com.commoncore.enums.privacy.Privacy;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserWorkplaceRequest {
    String id;

    String companyName;
    String position;
    String city;
    String description;

    Boolean isCurrent;
    Privacy privacy;
}