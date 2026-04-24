package com.notificationservice.dto.request;

import com.notificationservice.enums.mail.StateSendMail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StateEmailRequest {
    private String email;
    private String code;
    private StateSendMail state;
}
