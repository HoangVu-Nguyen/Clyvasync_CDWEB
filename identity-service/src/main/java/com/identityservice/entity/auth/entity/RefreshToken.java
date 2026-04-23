package com.identityservice.entity.auth.entity;


import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("refresh_tokens") // Tương đương @Table
public class RefreshToken {

    @TableId(type = IdType.ASSIGN_ID) // Dùng Snowflake ID (String) thay cho Long Identity
    private String id;

    @TableField("token")
    private String token;

    private String email;

    @TableField("expiry_date")
    private Instant expiryDate;

    @TableField("device_id")
    private String deviceId;

    @TableField("ip_address")
    private String ipAddress;

    // Logic Delete hoặc Trạng thái thu hồi
    // Nếu bạn muốn dùng cơ chế xoay vòng và vô hiệu hóa token cũ
    private boolean revoked;

    @TableField(fill = FieldFill.INSERT)
    private Instant createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;
}