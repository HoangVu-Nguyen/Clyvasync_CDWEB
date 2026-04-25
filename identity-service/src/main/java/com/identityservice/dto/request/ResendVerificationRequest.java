package com.identityservice.dto.request;

import com.commoncore.enums.otp.OtpType;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    private String email;
    private OtpType type;
}
