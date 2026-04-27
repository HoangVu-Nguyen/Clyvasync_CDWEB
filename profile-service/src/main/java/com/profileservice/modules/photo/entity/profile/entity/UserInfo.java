package com.profileservice.modules.photo.entity.profile.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.commoncore.enums.privacy.Privacy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_infos")
public class UserInfo {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("user_id")
    private String userId;
    @TableField("privacy")
    private Privacy privacy;
    @TableField("username")
    private String username;

    @TableField("bio")
    private String bio;

    @TableField("location")
    private String location;
    @TableField("avatar_url")
    private String avatarUrl;
    @TableField("cover_url")
    private String coverUrl;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}