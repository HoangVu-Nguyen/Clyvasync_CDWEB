package com.mediaservice.modules.photo.consumer;


import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.UserEvent;
import com.mediaservice.modules.photo.handler.MediaEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
public class MediaEventConsumer {

    private final MediaEventHandler mediaEventHandler;

    @KafkaListener(
            topics = KafkaConstant.USER_EVENTS_TOPIC,
            groupId = "media-group"
    )
    public void listenUserEvents(BaseEvent<UserEvent> event) {
        log.info(">>>> [KAFKA MEDIA] Processing event: {} for user: {}",
                event.getType(), event.getPayload().getUserId());

        switch (event.getType()) {
            case USER_REGISTERED:
                mediaEventHandler.initializeUserPhotos(event.getPayload());
                break;

            default:
                log.debug(">>>> [KAFKA] Media service ignoring event: {}", event.getType());
                break;
        }
    }
}