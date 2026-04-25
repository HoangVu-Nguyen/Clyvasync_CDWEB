package com.commoncore.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserEventDTO implements Serializable {
    // Nhóm 1: Dùng cho cả Email và Profile
    private String email;
    private String type; // REGISTER_OTP, FORGOT_PASSWORD, USER_REGISTERED...

    // Nhóm 2: Dùng riêng cho gửi Mail (OTP)
    private String code;

    // Nhóm 3: Dùng riêng cho đồng bộ Profile (Onboarding)
    private String userId;
    private String username;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;
}