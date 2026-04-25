package com.identityservice.producer;



import com.commoncore.dto.event.UserEventDTO;
import com.identityservice.dto.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class AuthProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void sendEvent(String topic, String key, T payload) {
        log.info(">>>> [KAFKA] Sending event to TOPIC: {} | Key: {}", topic, key);

        kafkaTemplate.send(topic, key, payload);

        log.info(">>>> [KAFKA] Message dispatched successfully!");
    }
}