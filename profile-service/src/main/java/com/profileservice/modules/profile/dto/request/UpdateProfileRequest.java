package com.profileservice.modules.profile.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateProfileRequest {

    @Size(max = 50, message = "Tên hiển thị không được vượt quá 50 ký tự")
    private String displayName;

    @Size(max = 255, message = "Tiểu sử (Bio) không được vượt quá 255 ký tự")
    private String bio;
    private String location;
    private String website;
    private String avatarUrl;

    private String coverUrl;
    List<UserEducationRequest> educations;
    List<UserWorkplaceRequest> workplaces;

}