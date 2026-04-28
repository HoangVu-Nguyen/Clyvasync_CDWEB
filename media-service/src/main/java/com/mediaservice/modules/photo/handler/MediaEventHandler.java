package com.mediaservice.modules.photo.handler;

import com.commoncore.dto.event.UserEvent;
import org.springframework.stereotype.Service;

public interface MediaEventHandler {
    void initializeUserPhotos(UserEvent payload);
}
