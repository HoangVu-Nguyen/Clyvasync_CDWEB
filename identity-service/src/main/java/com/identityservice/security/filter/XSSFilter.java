package com.identityservice.security.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class XSSFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Kiểm tra các Parameter
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (String[] values : parameterMap.values()) {
            for (String value : values) {
                if (!Jsoup.isValid(value, Safelist.none())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("XSS Attack Detected in Parameters!");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}