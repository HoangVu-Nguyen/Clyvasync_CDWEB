package com.mediaservice.modules.photo.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.commoncore.enums.photo.ImageType;
import com.commoncore.enums.privacy.Privacy;
import com.commoncore.enums.status.PhotoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_photos")
public class UserPhoto {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("user_id")
    private String userId;

    @TableField("photo_url")
    private String photoUrl;

    @TableField("photo_type")
    private ImageType photoType;
    @TableField("status")
    @Builder.Default
    private PhotoStatus status = PhotoStatus.PENDING;

    @TableField("privacy")
    @Builder.Default
    private Privacy privacy = Privacy.PUBLIC;

    @TableField("is_current")
    @Builder.Default
    private Boolean isCurrent = false;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}