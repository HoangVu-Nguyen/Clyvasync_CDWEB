package com.identityservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LoginResponse {
        private String accessToken;
        private String refreshToken;

}
