package com.commoncore.dto.event;

import com.commoncore.enums.event.EventType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent<T> implements Serializable {
    private String eventId;
    private EventType type;
    private LocalDateTime createdAt;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private T payload;
}