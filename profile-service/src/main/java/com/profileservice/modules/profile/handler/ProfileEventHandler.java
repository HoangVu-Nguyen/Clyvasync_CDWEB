package com.profileservice.modules.profile.handler;

import com.commoncore.dto.event.UserEvent;

public interface ProfileEventHandler {
    void handleRegistration(UserEvent payload);
}