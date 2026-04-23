package com.identityservice.enums.otp;


import lombok.Getter;

@Getter
public enum OtpType {
    ACTIVATION("Kích hoạt tài khoản Warp Zone"),
    RECOVERY("Khôi phục Secret Key");

    private final String emailSubject;

    OtpType(String emailSubject) {
        this.emailSubject = emailSubject;
    }

}
