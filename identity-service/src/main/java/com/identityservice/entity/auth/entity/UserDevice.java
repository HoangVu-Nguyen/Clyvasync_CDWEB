package com.identityservice.entity.auth.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("user_devices")
public class UserDevice {

    @TableId(type = IdType.ASSIGN_ID) // Đồng bộ dùng Snowflake ID (String)
    private String id;

    @TableField("user_id")
    private String userId; // Đổi sang String để khớp với UserCredential.id

    @TableField("refresh_token_id")
    private String refreshTokenId; // Lưu ID thay vì dùng @OneToOne của JPA

    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String location;

    private LocalDateTime lastActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}