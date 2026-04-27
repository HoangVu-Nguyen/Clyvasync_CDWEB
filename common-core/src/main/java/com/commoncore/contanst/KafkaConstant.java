package com.commoncore.contanst;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE) // Chặn việc new KafkaConstant()
public class KafkaConstant {
    public static final String USER_REGISTERED = "identity.user.registered";
    public static final String USER_ACTIVATED = "identity.user.activated";
    public static final String FORGOT_PASSWORD = "identity.user.forgot-password";
    public static final String USER_EVENTS_TOPIC = "user-events-topic";
    public static final String MEDIA_UPDATE_TOPIC = "media-update-topic";
}