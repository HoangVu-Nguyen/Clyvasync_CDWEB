package com.commonsecurity.config;


import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/profiles/**").permitAll() // Tạm thời permitAll để test, sau này đổi thành authenticated()
                        .requestMatchers("/api/v1/media/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // 1. Tạo một RestTemplate bỏ qua kiểm tra chứng chỉ SSL
        RestTemplate restTemplate = new RestTemplate();

        try {
           TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
            org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory sslsf =
                    new org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory(
                            org.apache.hc.core5.ssl.SSLContexts.custom()
                                    .loadTrustMaterial(null, acceptingTrustStrategy)
                                    .build(),
                            org.apache.hc.client5.http.ssl.NoopHostnameVerifier.INSTANCE);

            org.apache.hc.client5.http.impl.classic.CloseableHttpClient httpClient =
                    org.apache.hc.client5.http.impl.classic.HttpClients.custom()
                            .setConnectionManager(
                                    org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder.create()
                                            .setSSLSocketFactory(sslsf)
                                            .build())
                            .build();

            restTemplate.setRequestFactory(new org.springframework.http.client.HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (Exception e) {
            throw new RuntimeException("Không thể cấu hình SSL bỏ qua kiểm tra", e);
        }

        // 2. Dùng RestTemplate này để fetch JWKS từ Gateway
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(this.jwkSetUri)
                .restOperations(restTemplate) // Gán cái RestTemplate "thoáng" này vào
                .build();

        return jwtDecoder;
    }


}