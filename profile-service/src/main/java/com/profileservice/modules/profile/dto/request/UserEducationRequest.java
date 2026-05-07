package com.profileservice.modules.profile.dto.request;

import com.commoncore.enums.privacy.Privacy;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEducationRequest {
    String  id;

    String schoolName;
    String degree;
    String major;
    String type;

    Privacy privacy;
}
