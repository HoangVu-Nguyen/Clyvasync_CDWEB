package com.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {
    @NotBlank(message = "DEVICE_ID_REQUIRED")
    private String deviceId;

}
