package com.identityservice.service.auth;

import com.identityservice.dto.request.LoginRequest;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.TokenResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    TokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String accessToken, String deviceId);

    void logoutAll(String token);


    // =====================================================================
    // 2. REGISTRATION & VERIFICATION (Đăng ký / Xác thực tài khoản)
    // =====================================================================

    void register(RegisterRequest request);

}
