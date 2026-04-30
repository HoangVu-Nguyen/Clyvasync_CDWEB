package com.profileservice.modules.profile.dto.event;

import com.commoncore.dto.event.MediaUpdateEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor

public class ProfileMediaCommitEvent {
    private final List<MediaUpdateEvent> events;
}