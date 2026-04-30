package com.profileservice.modules.profile.listener;

import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.MediaUpdateEvent;
import com.commoncore.enums.event.EventType;
import com.commoncore.enums.photo.ImageType;
import com.commoncore.producer.CoreKafkaProducer;
import com.profileservice.modules.profile.dto.event.ProfileMediaCommitEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@RequiredArgsConstructor
@Slf4j
@Component
public class ProfileCommitListener {
    private final CoreKafkaProducer coreKafkaProducer;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMediaUpdateAfterCommit(ProfileMediaCommitEvent event) {
        log.info("Transaction DB thành công! Bắt đầu bắn {} event sang Media Service", event.getEvents().size());

        for (MediaUpdateEvent mediaPayload : event.getEvents()) {
            try {
                BaseEvent<MediaUpdateEvent> kafkaEvent = new BaseEvent<>();
                kafkaEvent.setType(mediaPayload.type().equals(ImageType.AVATAR) ? EventType.AVATAR_UPDATED : EventType.COVER_UPDATED);
                kafkaEvent.setPayload(mediaPayload);

                coreKafkaProducer.sendEvent(
                        KafkaConstant.MEDIA_UPDATE_TOPIC,
                        mediaPayload.userId(),
                        kafkaEvent
                );

                log.info("Đã gửi Kafka confirm ảnh {} cho user: {}", mediaPayload.type(), mediaPayload.userId());
            } catch (Exception e) {
                log.error("Lỗi khi gửi Kafka cho user {}: {}", mediaPayload.userId(), e.getMessage());
            }
        }
    }
}
