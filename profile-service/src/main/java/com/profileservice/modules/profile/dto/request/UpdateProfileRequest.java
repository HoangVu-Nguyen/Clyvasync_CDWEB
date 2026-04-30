package com.profileservice.modules.profile.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 50, message = "Tên hiển thị không được vượt quá 50 ký tự")
    private String displayName;

    @Size(max = 255, message = "Tiểu sử (Bio) không được vượt quá 255 ký tự")
    private String bio;
    private String location;
    private String website;



    private String avatarUrl;

    private String coverUrl;
}