package com.identityservice.dto.request;

import com.commonlibrary.enums.otp.OtpType;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    private String email;
    private OtpType type;
}
