package com.identityservice.security.custom;


import com.commonlibrary.enums.otp.OtpType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");
        if (email == null) email = "";

        if (exception instanceof DisabledException) {
            log.warn("Tài khoản chưa kích hoạt cố gắng đăng nhập: {}", email);

            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

            String targetUrl = "/verify-otp?email=" + encodedEmail + "&type="+ OtpType.ACTIVATION.name();
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } else {
            log.warn("Đăng nhập thất bại: {}. Lỗi: {}", email, exception.getMessage());
            getRedirectStrategy().sendRedirect(request, response, "/login?error=true");
        }
    }
}