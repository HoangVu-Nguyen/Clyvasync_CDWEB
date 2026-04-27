package com.profileservice.modules.photo.consumer;


import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.UserEvent;
import com.profileservice.modules.photo.handler.ProfileEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProfileEventConsumer {

    private final ProfileEventHandler profileEventHandler;

    @KafkaListener(
            topics = KafkaConstant.USER_EVENTS_TOPIC,
            groupId = "profile-group"
    )
    public void listenUserEvents(BaseEvent<UserEvent> event) {
        log.info(">>>> [KAFKA PROFILE] Received event: {} with ID: {}", event.getType(), event.getEventId());

        switch (event.getType()) {
            case USER_REGISTERED:
                profileEventHandler.handleRegistration(event.getPayload());
                break;

            case USER_UPDATE:
                // Sau này ông làm tính năng đổi tên bên Identity thì xử lý ở đây
                break;

            default:
                log.debug(">>>> [KAFKA] Ignore irrelevant event type: {}", event.getType());
                break;
        }
    }
}