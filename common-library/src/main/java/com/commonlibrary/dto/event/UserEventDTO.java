package com.commonlibrary.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserEventDTO implements Serializable {
    private String email;
    private String fullName;
    private String code;
    private String type;
}
