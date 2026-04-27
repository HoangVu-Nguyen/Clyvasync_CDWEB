package com.profileservice.modules.photo.dto.response;


import com.commoncore.enums.photo.ImageType;
import com.commoncore.enums.privacy.Privacy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPhotoResponse implements Serializable {
    private Long id;

    private Long userId;

    private String photoUrl;
    private Privacy privacy;

    private ImageType photoType;

    private Boolean isCurrent;

    private LocalDateTime createdAt;


}

