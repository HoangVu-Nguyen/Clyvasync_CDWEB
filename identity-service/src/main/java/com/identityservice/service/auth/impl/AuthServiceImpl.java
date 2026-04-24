package com.identityservice.service.auth.impl;


import com.commonlibrary.contanst.KafkaConstant;
import com.commonlibrary.dto.event.UserEventDTO;
import com.commonlibrary.enums.otp.OtpType;
import com.commonlibrary.exception.AppException;
import com.commonlibrary.exception.ResultCode;
import com.identityservice.dto.request.LoginRequest;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.dto.request.ResendVerificationRequest;
import com.identityservice.dto.request.VerifyAccountRequest;
import com.identityservice.dto.response.TokenResponse;
import com.identityservice.entity.auth.entity.UserCredential;
import com.identityservice.enums.auth.RoleName;
import com.identityservice.enums.cache.RedisKeyType;
import com.identityservice.producer.AuthProducer;
import com.identityservice.security.password.PasswordService;
import com.identityservice.security.util.JwtUtil;
import com.identityservice.service.auth.AuthService;
import com.identityservice.service.auth.RefreshTokenService;
import com.identityservice.service.auth.UserRoleService;
import com.identityservice.service.auth.UserService;
import com.identityservice.service.cache.CacheService;
import com.identityservice.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final CacheService cacheService;
    private final UserService userService; // Đảm bảo userService sử dụng UserCredentialMapper
    private final PasswordService passwordService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService; // Cần cái này để lấy/gán Role
    private final OtpService otpService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthProducer authProducer; // RabbitMQ Producer

    @Override
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Kiểm tra khóa tài khoản (Brute force protection)
        if (cacheService.isAccountLocked(email)) {
            throw new AppException(ResultCode.ACCOUNT_TEMPORARILY_LOCKED);
        }

        // 2. Tìm User và nạp kèm Roles (Vì JWT cần danh sách Role)
        UserCredential user = userService.findOptionalByEmail(email)
                .orElseThrow(() -> {
                    cacheService.increaseFailedAttempts(email);
                    return new AppException(ResultCode.LOGIN_FAILED);
                });

        // 3. Kiểm tra mật khẩu (Sử dụng PasswordService có Pepper)
        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {
            cacheService.increaseFailedAttempts(email);
            throw new AppException(ResultCode.LOGIN_FAILED);
        }

        // 4. Kiểm tra trạng thái xác thực
        if (!user.isVerified()) {
            throw new AppException(ResultCode.USER_NOT_ACTIVE);
        }

        // 5. Thành công -> Reset đếm lỗi & Lấy Roles thực tế từ DB
        cacheService.resetFailedAttempts(email);
        Set<String> roles = userRoleService.findRolesByUserId(user.getId());

        // 6. Tạo Token pair
        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), roles, user.getId());
        String refreshToken = refreshTokenService.issueRefreshToken(
                user.getEmail(), request.getDeviceId(), userAgent, ipAddress
        );

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
    @Transactional // QUAN TRỌNG: Đảm bảo lưu User và Role là một khối
    public void register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Chặn spam ngay từ cửa
        if (cacheService.isSpamming(email, RedisKeyType.SEND_EMAIL_LIMIT)) {
            throw new AppException(ResultCode.PLEASE_WAIT_BEFORE_RESENDING);
        }

        validateRegisterRequest(request);

        UserCredential userToSave;
        Optional<UserCredential> userOptional = userService.findOptionalByEmail(email);

        if (userOptional.isPresent()) {
            UserCredential existingUser = userOptional.get();
            if (existingUser.isVerified()) {
                throw new AppException(ResultCode.USER_EXISTED);
            }
            userToSave = updateUserFromRequest(existingUser, request);
        } else {
            userToSave = createUserFromRequest(request);
        }

        userService.save(userToSave);

        // 3. Gán Role mặc định (Bây giờ đã có userToSave.getId())
        userRoleService.assignDefaultRole(userToSave.getId(), RoleName.USER);

        // 4. Xử lý OTP
        String otp = otpService.generateOtp();
        cacheService.saveOtp(email, otp, RedisKeyType.VERIFY_ACCOUNT);
        cacheService.setProcessLimit(email, RedisKeyType.SEND_EMAIL_LIMIT);

        // 5. Thông báo hệ thống & Gửi RabbitMQ
       // eventPublisher.publishEvent(new UserRegisteredEvent(userToSave));

        UserEventDTO eventPayload = UserEventDTO.builder()
                .email(email)
                .fullName(userToSave.getName())
                .code(otp)
                .type(OtpType.ACTIVATION.name())
                .build();
        authProducer.sendEvent(KafkaConstant.USER_REGISTERED,eventPayload);

        log.info("Đăng ký thành công, OTP đã gửi tới: {}", email);
    }

    // --- Helper Methods ---

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ResultCode.PASSWORD_NOT_MATCH);
        }
        // passwordService sẽ check độ mạnh dựa trên regex/policy
        if (!passwordService.isStrongPassword(request.getPassword())) {
            throw new AppException(ResultCode.PASSWORD_TOO_WEAK);
        }
    }

    private UserCredential createUserFromRequest(RegisterRequest request) {
        UserCredential user = new UserCredential();

        // Chuẩn hóa dữ liệu đầu vào
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setName(request.getUsername().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Trạng thái mặc định
        user.setVerified(false);
        user.setStatus(1);

        // createdAt/updatedAt sẽ do MyMetaObjectHandler lo (nếu ông đã cấu hình)
        return user;
    }

    private UserCredential updateUserFromRequest(UserCredential user, RegisterRequest request) {
        // Chỉ cập nhật nếu có dữ liệu mới
        if (StringUtils.hasText(request.getUsername())) {
            user.setName(request.getUsername().trim());
        }

        if (StringUtils.hasText(request.getPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Nếu ông dùng @TableField(fill = FieldFill.INSERT_UPDATE)
        // thì KHÔNG CẦN dòng setUpdatedAt thủ công này nữa, MyBatis Plus tự lo.
        // user.setUpdatedAt(LocalDateTime.now());

        return user;
    }
    @Override
    @Transactional
    public void verifyAccount(VerifyAccountRequest request) {
        String email = request.getEmail();

        UserCredential user = userService.findOptionalByEmail(email)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));


        if (user.isVerified()) {
            cacheService.delete(RedisKeyType.VERIFY_ACCOUNT.getFullKey(email));
            throw new AppException(ResultCode.USER_ALREADY_ACTIVE);
        }

        String storedToken = cacheService.getOtp(email, RedisKeyType.VERIFY_ACCOUNT);
        if (ObjectUtils.isEmpty(storedToken)) {
            throw new AppException(ResultCode.OTP_EXPIRED);
        }

        if (!storedToken.equals(request.getCode())) {
            throw new AppException(ResultCode.OTP_INVALID);
        }

        user.setVerified(true);
        user.setVerifiedAt(LocalDateTime.now());
        userService.save(user);

        cacheService.delete(RedisKeyType.VERIFY_ACCOUNT.getFullKey(email));
        cacheService.delete(RedisKeyType.USER_PROFILE.getFullKey(email));

        log.info("Xác thực tài khoản thành công cho email: {}", email);
    }
    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        OtpType type = request.getType(); // Lấy Enum

        if (cacheService.isSpamming(email, RedisKeyType.SEND_EMAIL_LIMIT)) {
            throw new AppException(ResultCode.PLEASE_WAIT_BEFORE_RESENDING);
        }

        UserCredential user = userService.findOptionalByEmail(email)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        if (type == OtpType.ACTIVATION) {
            if (user.isVerified()) throw new AppException(ResultCode.USER_ALREADY_ACTIVE);
        } else if (type == OtpType.RECOVERY) {
            if (!user.isVerified()) throw new AppException(ResultCode.USER_NOT_ACTIVE);
        }

        String otp = otpService.generateOtp();
        cacheService.saveOtp(email, otp, RedisKeyType.VERIFY_ACCOUNT);
        cacheService.setProcessLimit(email, RedisKeyType.SEND_EMAIL_LIMIT);


        UserEventDTO eventPayload = new UserEventDTO(user.getEmail(), user.getName(), otp, type.name());
        authProducer.sendEvent(KafkaConstant.USER_ACTIVATED,eventPayload);

        log.info("Đã gửi lại OTP (Type: {}) thành công cho: {}", type.name(), email);
    }

    @Override
    public void verifyPasswordResetOtp(String otp, String email) {
        String cleanEmail = email.trim().toLowerCase();

        userService.findOptionalByEmail(cleanEmail)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        String storedOtp = cacheService.getOtp(cleanEmail, RedisKeyType.VERIFY_ACCOUNT);
        if (ObjectUtils.isEmpty(storedOtp)) {
            throw new AppException(ResultCode.OTP_EXPIRED);
        }

        if (!storedOtp.equals(otp)) {
            throw new AppException(ResultCode.OTP_INVALID);
        }

        log.info("Xác thực mã OTP khôi phục hợp lệ cho email: {}", cleanEmail);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        String cleanEmail = email.trim().toLowerCase();

        UserCredential user = userService.findOptionalByEmail(cleanEmail)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        // 2. Chống spam gửi mail
        if (cacheService.isSpamming(cleanEmail, RedisKeyType.SEND_EMAIL_LIMIT)) {
            throw new AppException(ResultCode.PLEASE_WAIT_BEFORE_RESENDING);
        }

        // 3. Tạo mã OTP khôi phục (Dùng chung Type VERIFY_ACCOUNT hoặc tạo Type mới FORGOT_PASSWORD)
        String otp = otpService.generateOtp();
        cacheService.saveOtp(cleanEmail, otp, RedisKeyType.VERIFY_ACCOUNT);
        cacheService.setProcessLimit(cleanEmail, RedisKeyType.SEND_EMAIL_LIMIT);

        // 4. Bắn vào RabbitMQ để gửi mail "Khôi phục mật khẩu"
        // Gợi ý: Bạn có thể tạo một Event riêng hoặc dùng chung RegisterEvent với template mail khác
        UserEventDTO eventPayload = new UserEventDTO(user.getEmail(), user.getName(), otp,OtpType.RECOVERY.name());
        authProducer.sendEvent(KafkaConstant.FORGOT_PASSWORD,eventPayload);

        log.info("Yêu cầu khôi phục mật khẩu cho email: {}", cleanEmail);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword, String otp) {
        String cleanEmail = email.trim().toLowerCase();

        UserCredential user = userService.findOptionalByEmail(cleanEmail)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));


        String storedOtp = cacheService.getOtp(cleanEmail, RedisKeyType.VERIFY_ACCOUNT);
        if (ObjectUtils.isEmpty(storedOtp)) {
            throw new AppException(ResultCode.OTP_EXPIRED);
        }
        if (!storedOtp.equals(otp)) {
            throw new AppException(ResultCode.OTP_INVALID);
        }

        if (!passwordService.isStrongPassword(newPassword)) {
            throw new AppException(ResultCode.PASSWORD_TOO_WEAK);
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new AppException(ResultCode.PASSWORD_NOT_MATCH);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userService.save(user);

        cacheService.delete(RedisKeyType.VERIFY_ACCOUNT.getFullKey(cleanEmail));
        refreshTokenService.revokeAllUserSessions(cleanEmail);

        log.info("STAGE CLEARED: Đã đặt lại mật khẩu thành công cho email: {}", cleanEmail);
    }

}