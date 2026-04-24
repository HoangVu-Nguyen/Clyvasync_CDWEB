package com.identityservice.producer;


import com.commonlibrary.contanst.KafkaConstant;
import com.commonlibrary.dto.event.UserEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class AuthProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEvent(String topic, UserEventDTO payload) {
        log.info(">>>> [KAFKA] Đang gửi event tới TOPIC: {} | Email: {}", topic, payload.getEmail());


        kafkaTemplate.send(topic, payload.getEmail(), payload);

        log.info(">>>> [KAFKA] Gửi xong!");
    }
}