package com.identityservice.service.auth.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.commonlibrary.exception.AppException;
import com.commonlibrary.exception.ResultCode;
import com.identityservice.dto.request.RefreshTokenRequest;
import com.identityservice.dto.response.TokenResponse;
import com.identityservice.entity.auth.entity.RefreshToken;
import com.identityservice.entity.auth.entity.UserDevice;
import com.identityservice.mapper.auth.RefreshTokenMapper;
import com.identityservice.mapper.auth.UserDeviceMapper;
import com.identityservice.security.util.JwtUtil;
import com.identityservice.service.auth.RefreshTokenService;
import com.identityservice.service.auth.UserService;
import com.identityservice.service.util.GeoIPService;
import com.identityservice.service.util.IPAddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenMapper refreshTokenMapper;
    private final UserDeviceMapper userDeviceMapper;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final GeoIPService geoIPService;

    @Override
    @Transactional
    public String issueRefreshToken(String email, String deviceId, String userAgent, String ipAddress) {
        log.info("Cấp Refresh Token mới cho: {} trên thiết bị: {}", email, deviceId);

        // 1. Dọn dẹp session cũ trên cùng thiết bị (Dùng LambdaUpdateWrapper)
        refreshTokenMapper.delete(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getEmail, email)
                .eq(RefreshToken::getDeviceId, deviceId));

        // 2. Tạo thực thể RefreshToken
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .email(email)
                .expiryDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .deviceId(deviceId)
                .revoked(false)
                .build();

        refreshTokenMapper.insert(refreshToken); // MyBatis-Plus tự điền ID vào object sau khi insert

        // 3. Cập nhật thông tin chi tiết thiết bị
        syncUserDevice(refreshToken, userAgent, ipAddress);

        return refreshToken.getToken();
    }

    @Override
    @Transactional
    public TokenResponse rotateTokens(RefreshTokenRequest request, String ipAddress, String userAgent) {
        // 1. Kiểm tra Token tồn tại
        RefreshToken oldToken = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getToken, request.getRefreshToken())
                .eq(RefreshToken::getDeviceId, request.getDeviceId())
                .eq(RefreshToken::getEmail, request.getEmail()));

        if (oldToken == null) throw new AppException(ResultCode.INVALID_TOKEN);

        // 2. Kiểm tra hiệu lực
        if (oldToken.getExpiryDate().isBefore(Instant.now()) || oldToken.isRevoked()) {
            deleteSessionInternal(oldToken);
            throw new AppException(ResultCode.TOKEN_EXPIRED);
        }

        // 3. Lấy thông tin User
        var user = userService.findOptionalByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        // 4. TOKEN ROTATION: Đổi mã mới
        String newTokenString = UUID.randomUUID().toString();
        oldToken.setToken(newTokenString);
        oldToken.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));

        refreshTokenMapper.updateById(oldToken);

        // 5. Đồng bộ metadata thiết bị
        syncUserDevice(oldToken, userAgent, ipAddress);

        // 6. Tạo Access Token mới (Nhớ truyền ID user để các service sau lấy được)
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles(), user.getId());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newTokenString)
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        RefreshToken tokenObj = refreshTokenMapper.selectOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getToken, token));
        if (tokenObj != null) deleteSessionInternal(tokenObj);
    }

    @Override
    @Transactional
    public void revokeAllUserSessions(String email) {
        log.warn("Force Logout toàn bộ cho: {}", email);
        // Lưu ý: Nếu DB không để ON DELETE CASCADE, bạn phải tự dọn bảng user_devices trước
        refreshTokenMapper.delete(new LambdaQueryWrapper<RefreshToken>().eq(RefreshToken::getEmail, email));
    }

    @Override
    @Transactional
    public void revokeOtherSessions(String email, String currentToken) {
        refreshTokenMapper.delete(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getEmail, email)
                .ne(RefreshToken::getToken, currentToken)); // .ne là Not Equal (!=)
    }

    @Override
    @Transactional
    public void deleteByDeviceIdAndEmail(String deviceId, String email) {
        refreshTokenMapper.delete(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getDeviceId, deviceId)
                .eq(RefreshToken::getEmail, email));
    }

    // --- Helpers ---

    private void syncUserDevice(RefreshToken token, String userAgent, String ipAddress) {
        // Tìm device theo ID của refresh token
        UserDevice device = userDeviceMapper.selectOne(new LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getRefreshTokenId, token.getId()));

        if (device == null) {
            device = new UserDevice();
            device.setCreatedAt(LocalDateTime.now());
        }

        var user = userService.findOptionalByEmail(token.getEmail())
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        device.setUserId(user.getId());
        device.setRefreshTokenId(token.getId());
        device.setDeviceName(IPAddressUtil.parseDeviceName(userAgent));
        device.setDeviceType(IPAddressUtil.parseDeviceType(userAgent));
        device.setIpAddress(ipAddress);
        device.setLocation(geoIPService.getLocationFromIp(ipAddress));
        device.setLastActive(LocalDateTime.now());

        // Nếu có ID thì update, không thì insert
        if (device.getId() == null) {
            userDeviceMapper.insert(device);
        } else {
            userDeviceMapper.updateById(device);
        }
    }

    private void deleteSessionInternal(RefreshToken token) {
        userDeviceMapper.delete(new LambdaQueryWrapper<UserDevice>()
                .eq(UserDevice::getRefreshTokenId, token.getId()));
        refreshTokenMapper.deleteById(token.getId());
    }
}