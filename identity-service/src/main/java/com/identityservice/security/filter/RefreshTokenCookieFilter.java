package com.identityservice.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // Chạy thật sớm, trước cả Spring Security
public class RefreshTokenCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ xử lý các request POST đến endpoint /oauth2/token
        if ("/oauth2/token".equals(request.getServletPath())
                && "refresh_token".equals(request.getParameter("grant_type"))) {

            String refreshTokenFromCookie = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("refresh_token".equals(cookie.getName())) {
                        refreshTokenFromCookie = cookie.getValue();
                        break;
                    }
                }
            }

            if (refreshTokenFromCookie != null) {
                // 🔥 ĐÂY LÀ PHÉP THUẬT: Tráo đổi chữ "true" bằng Token thật từ Cookie
                final String finalToken = refreshTokenFromCookie;
                HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
                    @Override
                    public String getParameter(String name) {
                        if ("refresh_token".equals(name)) {
                            return finalToken;
                        }
                        return super.getParameter(name);
                    }

                    @Override
                    public String[] getParameterValues(String name) {
                        if ("refresh_token".equals(name)) {
                            return new String[]{finalToken};
                        }
                        return super.getParameterValues(name);
                    }

                    @Override
                    public Map<String, String[]> getParameterMap() {
                        Map<String, String[]> map = new HashMap<>(super.getParameterMap());
                        map.put("refresh_token", new String[]{finalToken});
                        return Collections.unmodifiableMap(map);
                    }
                };
                filterChain.doFilter(wrapper, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}