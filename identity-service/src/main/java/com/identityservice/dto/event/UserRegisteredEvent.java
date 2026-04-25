package com.identityservice.dto.event;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserRegisteredEvent {
    private Long userId;
    private String email;
    private String username;
    private String phoneNumber;
    private LocalDate birthDate;
    private String gender;
    private String type;
}