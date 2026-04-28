package com.mediaservice.modules.photo.dto.request;

import com.commoncore.enums.photo.ImageType;
import lombok.Data;

@Data
public class UploadRequest {
    private String fileName;    // Ví dụ: my-photo.jpg
    private String contentType; // Ví dụ: image/jpeg hoặc image/png
    private ImageType imageType;   // Dùng để phân loại: AVATAR, COVER, hoặc POST
}