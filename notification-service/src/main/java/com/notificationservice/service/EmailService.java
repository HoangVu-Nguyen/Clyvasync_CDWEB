package com.notificationservice.service;

import com.notificationservice.dto.request.StateEmailRequest;

public interface EmailService {
    void sendStateEmail(StateEmailRequest request);
}
