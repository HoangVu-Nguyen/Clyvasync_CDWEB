package com.commonsecurity.config;



import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
@Validated // Đảm bảo cấu hình truyền vào phải đúng quy tắc
public class JwtProperties {

    @NotBlank(message = "JWT Secret cannot be empty")
    private String secret;

    @Min(value = 60000, message = "Access Token expiration must be at least 1 minute")
    private long accessTokenExpiration;

    @Min(value = 86400000, message = "Refresh Token expiration must be at least 1 day")
    private long refreshTokenExpiration;
}