package com.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginHistoryResponse {
    private Long id;
    private String deviceName;
    private String location;
    private LocalDateTime lastActive;
    private boolean isCurrentDevice;
    private String iconType;
    private String ipAddress;
}
