package com.identityservice.entity.auth.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@TableName(value = "user_credential", autoResultMap = true) // Cần autoResultMap để dùng TypeHandler
public class UserCredential {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;


    private String email;


    @TableField("password")
    private String passwordHash;

    @TableField("is_verified")
    private boolean isVerified;

    @TableLogic(value = "1", delval = "0")
    private Integer status;
    @TableField(exist = false)
    private Set<String> roles;

    private LocalDateTime verifiedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}