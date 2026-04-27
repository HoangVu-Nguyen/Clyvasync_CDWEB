package com.profileservice.modules.profile.entity.profile.entity;

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
@TableName("user_educations")
public class UserEducation {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String  id;

    @TableField("user_id")
    private String userId;

    @TableField("school_name")
    private String schoolName;

    @TableField("type")
    private String type; // Có thể dùng Enum EducationType nếu muốn chặt chẽ

    @TableField("major")
    private String major;

    @TableField("degree")
    private String degree;

    @TableField("start_date")
    private LocalDate startDate;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("is_graduated")
    private Boolean isGraduated;

    @TableField("privacy")
    private Privacy privacy;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}