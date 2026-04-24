package com.commonlibrary.contanst;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // Chặn việc new KafkaConstant()
public class KafkaConstant {
    public static final String USER_REGISTERED = "identity.user.registered";
    public static final String USER_ACTIVATED = "identity.user.activated";
    public static final String FORGOT_PASSWORD = "identity.user.forgot-password";
}