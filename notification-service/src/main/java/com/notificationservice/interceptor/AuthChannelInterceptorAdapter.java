package com.notificationservice.interceptor;

import com.commoncore.exception.AppException;
import com.commoncore.exception.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthChannelInterceptorAdapter implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            String token = null;

            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                try {
                     token = authHeader.substring(7);
                    Jwt jwt = jwtDecoder.decode(token);

                    String userId = jwt.getClaimAsString("user_id");

                    accessor.setUser(() -> userId);

                    log.info(">>>> WebSocket Authenticated: User {}", userId);
                } catch (Exception e) {
                    log.error(">>>> WebSocket Auth Error: {}", e.getMessage());
                    return null;
                }
            }
            else {

                token = accessor.getFirstNativeHeader("token");
            }
        }
        return message;
    }
}