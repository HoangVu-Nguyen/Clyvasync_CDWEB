package com.identityservice.entity.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@TableName("roles")
public class Role {
    @TableId(type = IdType.AUTO) // Role thường ít, dùng Auto Increment là ổn
    private Integer id;

    private String name; // Lưu "ADMIN", "USER", ...
    private String description;
}