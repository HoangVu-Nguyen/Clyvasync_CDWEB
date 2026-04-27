package com.profileservice.modules.profile.entity.profile.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.commoncore.enums.privacy.Privacy;
import com.commoncore.enums.status.RelationshipStatus;
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

    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    @TableField("username")
    private String username;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("bio")
    private String bio;

    @TableField("location")
    private String location;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("website")
    private String website;

    @TableField("relationship_status")
    private RelationshipStatus relationshipStatus;

    @TableField("hometown")
    private String hometown;

    @TableField("current_city")
    private String current_city;

    @TableField("privacy")
    private Privacy privacy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}