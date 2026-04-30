package com.mediaservice.modules.photo.dto.response;

import com.commoncore.enums.photo.ImageType;
import com.commoncore.enums.privacy.Privacy;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class PhotoResponse {
    private Long id;

    private Long userId;

    private String photoUrl;
    private Privacy privacy;

    private ImageType photoType;

    private Boolean isCurrent;

    private LocalDateTime createdAt;

}
