package com.identityservice.service.auth;


import com.identityservice.dto.request.RefreshTokenRequest;
import com.identityservice.dto.response.TokenResponse;

public interface RefreshTokenService {

    /**
     * Cấp mới một Refresh Token (Dùng khi Login thành công)
     * Thay 'generate' bằng 'issue' (cấp phát) chuẩn nghiệp vụ Security hơn.
     */
    String issueRefreshToken(String email, String deviceId, String userAgent, String ipAddress);

    /**
     * Làm mới cặp Token (Access & Refresh) - Đổi 'refreshToken' thành 'rotateTokens'
     * để làm rõ cơ chế Token Rotation.
     */
    TokenResponse rotateTokens(RefreshTokenRequest request, String ipAddress, String userAgent);

    /**
     * Thu hồi một phiên làm việc cụ thể (Logout thiết bị hiện tại)
     */
    void revokeToken(String token);

    /**
     * Thu hồi toàn bộ phiên làm việc của người dùng (Force Logout all devices)
     */
    void revokeAllUserSessions(String email);

    /**
     * Bảo mật: Đăng xuất khỏi mọi thiết bị khác trừ phiên đang dùng
     */
    void revokeOtherSessions(String email, String currentToken);

    void deleteByDeviceIdAndEmail(String deviceId, String email);
}