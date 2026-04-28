package com.identityservice.config;


import com.identityservice.security.custom.CustomUserDetails;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate; // Thêm JdbcTemplate
    @Value("${clyvasync.auth.client-id}")
    private String clientId;

    @Value("${clyvasync.auth.client-secret}")
    private String clientSecret;

    @Value("${clyvasync.auth.redirect-uri}")
    private String redirectUri;

    @Value("${clyvasync.auth.post-logout-uri}")
    private String postLogoutUri;

    /**
     * CHAIN 1: Giao thức OAuth2
     * Xử lý các endpoint: /.well-known/openid-configuration, /oauth2/authorize, /oauth2/token, /oauth2/jwks
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults())



                .tokenEndpoint(tokenEndpoint -> tokenEndpoint.accessTokenResponseHandler((request, response, authentication) -> {
                    if (authentication instanceof OAuth2AccessTokenAuthenticationToken tokenAuth) {
                        var accessToken = tokenAuth.getAccessToken();
                        var refreshToken = tokenAuth.getRefreshToken();

                        System.out.println("DEBUG: Access Token Value: " + accessToken.getTokenValue());
                        if (refreshToken != null) {
                            System.out.println("DEBUG: Refresh Token Value (Gửi về Client): " + refreshToken.getTokenValue());
                        } else {
                            System.out.println("DEBUG: Refresh Token là NULL - Kiểm tra lại scope offline_access hoặc Grant Type");
                        }
                        // ----------------------------

                        String idTokenValue = "";
                        if (tokenAuth.getAdditionalParameters().containsKey("id_token")) {
                            idTokenValue = tokenAuth.getAdditionalParameters().get("id_token").toString();
                        }

                        if (refreshToken != null) {
                            String cookieValue = "refresh_token=" + refreshToken.getTokenValue()
                                    + "; HttpOnly; Secure;Path=/; Max-Age=" + (60 * 60 * 24 * 30) + "; SameSite=Lax";
                            response.addHeader("Set-Cookie", cookieValue);
                        }

                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding("UTF-8");

                        long expiresIn = accessToken.getExpiresAt().getEpochSecond() - accessToken.getIssuedAt().getEpochSecond();

                        StringBuilder json = new StringBuilder();
                        json.append("{");
                        json.append("\"access_token\": \"").append(accessToken.getTokenValue()).append("\",");
                        if (!idTokenValue.isEmpty()) {
                            json.append("\"id_token\": \"").append(idTokenValue).append("\",");
                        }
                        if (refreshToken != null) {
                            json.append("\"refresh_token\": \"").append(refreshToken.getTokenValue()).append("\",");
                        }
                        json.append("\"token_type\": \"Bearer\",");
                        json.append("\"expires_in\": ").append(expiresIn);
                        json.append("}");

                        response.getWriter().write(json.toString());
                    }
                }));


        http.exceptionHandling((exceptions) ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        ))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }



    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);

        if (registeredClientRepository.findByClientId(clientId) == null) {
            RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId(clientId)
                    .clientSecret(passwordEncoder.encode(clientSecret))
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .redirectUri(redirectUri)
                    .postLogoutRedirectUri(postLogoutUri)
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .scope(OidcScopes.EMAIL)
                    .scope("offline_access")
                    .clientSettings(ClientSettings.builder()
                            .requireProofKey(true)
                            .requireAuthorizationConsent(true)
                            .build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(60))
                            .refreshTokenTimeToLive(Duration.ofDays(30))
                            .reuseRefreshTokens(true)
                            .build())
                    .build();
            registeredClientRepository.save(oidcClient);
        }
        return registeredClientRepository;
    }

    /**
     * Đưa thêm ID và Role vào JWT Token
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            Authentication auth = context.getPrincipal();

            Set<String> authorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            context.getClaims().claim("roles", authorities);

            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                context.getClaims().claim("user_id", userDetails.getId());
                context.getClaims().claim("email", userDetails.getEmail());
            } else {
                context.getClaims().claim("user_id", auth.getName());
            }
        };
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("https://localhost:8443")
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

}