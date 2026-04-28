package com.mediaservice.modules.photo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PresignedUrlResponse {
    private String uploadUrl; // URL tạm thời dùng để PUT ảnh lên S3
    private String objectKey; // Đường dẫn tương đối (ví dụ: users/1/avatars/abc.jpg) để lưu vào DB
}