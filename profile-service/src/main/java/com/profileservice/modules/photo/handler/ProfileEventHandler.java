package com.profileservice.modules.photo.handler;

import com.commoncore.dto.event.UserEvent;

public interface ProfileEventHandler {
    void handleRegistration(UserEvent payload);
}