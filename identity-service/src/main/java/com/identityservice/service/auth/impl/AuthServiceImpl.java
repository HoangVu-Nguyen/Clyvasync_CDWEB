package com.identityservice.service.auth.impl;


import com.commonlibrary.exception.AppException;
import com.commonlibrary.exception.ResultCode;
import com.identityservice.dto.request.LoginRequest;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.response.TokenResponse;
import com.identityservice.entity.auth.entity.UserCredential;
import com.identityservice.security.password.PasswordService;
import com.identityservice.security.password.impl.PasswordServiceImpl;
import com.identityservice.security.util.JwtUtil;
import com.identityservice.service.auth.AuthService;
import com.identityservice.service.auth.RefreshTokenService;
import com.identityservice.service.auth.UserService;
import com.identityservice.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final CacheService cacheService;
    private final UserService userService;
    private final PasswordService passwordService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @Override
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().trim().toLowerCase();
        if (cacheService.isAccountLocked(email)) {
            log.warn("Truy cập bị chặn: Tài khoản {} đang bị khóa", email);
            throw new AppException(ResultCode.ACCOUNT_TEMPORARILY_LOCKED);
        }
        if (cacheService.isBruteForce(email)) {
            if (!StringUtils.hasText(request.getRecaptcha())) {
                throw new AppException(ResultCode.CAPTCHA_REQUIRED);
            }
            // verifyCaptcha(request.getRecaptcha());
        }
        UserCredential user = userService.findOptionalByEmail(email)
                .orElseThrow(() -> {
                    cacheService.increaseFailedAttempts(email);
                    return new AppException(ResultCode.LOGIN_FAILED);
                });
        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {

            cacheService.increaseFailedAttempts(email);
            throw new AppException(ResultCode.LOGIN_FAILED);
        }

        if (!user.isVerified()) {
            throw new AppException(ResultCode.USER_NOT_ACTIVE);
        }
        cacheService.resetFailedAttempts(email);
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles(), user.getId());

        String refreshToken = refreshTokenService.issueRefreshToken(
                user.getEmail(),
                request.getDeviceId(),
                userAgent,
                ipAddress
        );

        log.info("User {} login thành công. Device: {}, IP: {}", email, request.getDeviceId(), ipAddress);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .build();

    }

    @Override
    public void logout(String accessToken, String deviceId) {

    }

    @Override
    public void logoutAll(String token) {

    }

    @Override
    public void register(RegisterRequest request) {

    }
}
