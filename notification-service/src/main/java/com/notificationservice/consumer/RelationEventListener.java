package com.notificationservice.consumer;

import com.commoncore.contanst.KafkaConstant;
import com.commoncore.contanst.NotificationConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.FriendRequestEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Slf4j
@Component
public class RelationEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = KafkaConstant.RELATION_OUT_EVENTS, groupId = "notification-group")
    public void handleRelationEvent(BaseEvent<FriendRequestEvent> payload) {
        log.info(">>>> [KAFKA RECEIVE] Received event: {} from {} to {}",
                payload.getType(), payload.getPayload().getFromUserId(), payload.getPayload().getToUserId());

        messagingTemplate.convertAndSendToUser(
                payload.getPayload().getToUserId(),
                NotificationConstant.USER_NOTIF_DESTINATION,
                payload.getPayload()
        );
//        messagingTemplate.convertAndSend(
//                "/topic/notifications/" + payload.getPayload().getToUserId(),
//                payload.getPayload()
//        );
    }
}
