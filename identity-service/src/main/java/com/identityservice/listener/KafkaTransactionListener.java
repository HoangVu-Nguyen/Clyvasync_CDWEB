package com.identityservice.listener;

import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.UserEvent;
import com.commoncore.producer.CoreKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaTransactionListener {

    private final CoreKafkaProducer coreKafkaProducer;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleKafkaPublishAfterCommit(BaseEvent<UserEvent> event) {

        String key = (event.getPayload().getUserId() != null)
                ? event.getPayload().getUserId()
                : event.getPayload().getEmail();

        coreKafkaProducer.sendEvent(KafkaConstant.USER_EVENTS_TOPIC, key, event);
    }
}