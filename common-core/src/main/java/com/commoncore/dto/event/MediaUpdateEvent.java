package com.commoncore.dto.event;

import com.commoncore.enums.photo.ImageType;
import lombok.Builder;
import lombok.Data;

@Builder

public record MediaUpdateEvent(
        String userId,
        String url,
        ImageType type,
        long timestamp
) {}