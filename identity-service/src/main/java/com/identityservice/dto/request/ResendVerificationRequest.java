package com.identityservice.dto.request;

import com.identityservice.enums.otp.OtpType;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    private String email;
    private OtpType type;
}
