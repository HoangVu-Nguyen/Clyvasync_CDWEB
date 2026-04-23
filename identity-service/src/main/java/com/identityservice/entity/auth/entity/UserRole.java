package com.identityservice.entity.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("user_roles")
public class UserRole {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;
    private Integer roleId;
}