package com.identityservice.security.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtEncoder jwtEncoder;




    public String generateAccessToken(String email, Set<String> roles, String userId) {
        Instant now = Instant.now();
        long expiry = 3600L; // 1 giờ

        String scope = roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .collect(Collectors.joining(" "));

        // Tạo bộ khung dữ liệu (Claims)
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://localhost:8443")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .subject(email)
                .claim("roles", roles)
                .claim("user_id", userId)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}