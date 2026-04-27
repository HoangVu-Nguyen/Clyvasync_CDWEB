package com.identityservice.listener;

import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.UserEvent;
import com.commoncore.producer.CoreKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaTransactionListener {

    private final CoreKafkaProducer coreKafkaProducer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleKafkaPublishAfterCommit(BaseEvent<?> event) {
        log.info(">>>> Đã nhận được event sau khi DB Commit thành công!");

        if (event.getPayload() instanceof UserEvent payload) {
            String key = (payload.getUserId() != null) ? payload.getUserId() : payload.getEmail();
            coreKafkaProducer.sendEvent(KafkaConstant.USER_EVENTS_TOPIC, key, (BaseEvent<UserEvent>) event);
        }
    }
}