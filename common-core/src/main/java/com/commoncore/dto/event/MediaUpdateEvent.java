package com.commoncore.dto.event;

import com.commoncore.enums.photo.ImageType;

public record MediaUpdateEvent(
        String userId,
        String url,
        ImageType type,
        long timestamp
) {}