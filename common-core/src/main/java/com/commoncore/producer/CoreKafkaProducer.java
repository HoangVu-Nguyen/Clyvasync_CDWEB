package com.commoncore.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CoreKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public <T> void sendEvent(String topic, String key, T payload) {
        log.info(">>>> [KAFKA] Đang gửi event vào TOPIC: {} | Key: {}", topic, key);
        kafkaTemplate.send(topic, key, payload);
        log.info(">>>> [KAFKA] Đã ném event thành công lên broker!");
    }
}