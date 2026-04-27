package com.commoncore.dto.event;

import com.commoncore.enums.event.EventType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore; // Import cái này
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor



public class BaseEvent<T> implements Serializable, ResolvableTypeProvider {
    private String eventId;
    private EventType type;
    private LocalDateTime createdAt;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private T payload;

    @Override
    @JsonIgnore // THÊM DÒNG NÀY VÀO ĐÂY
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(payload));
    }
}