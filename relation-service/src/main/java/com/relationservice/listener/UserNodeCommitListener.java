package com.relationservice.listener;


import com.commoncore.contanst.KafkaConstant;import com.commoncore.dto.event.BaseEvent;import com.commoncore.dto.event.FriendRequestEvent;import com.commoncore.enums.event.EventType;import com.commoncore.producer.CoreKafkaProducer;import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;import org.springframework.stereotype.Component;import org.springframework.transaction.event.TransactionPhase;import org.springframework.transaction.event.TransactionalEventListener;

@AllArgsConstructor
@Slf4j
@Component
public class UserNodeCommitListener {
    private final CoreKafkaProducer coreKafkaProducer;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFriendRequestCommit(FriendRequestEvent event) {
        log.info(">>>> [TRANSACTION COMMIT] DB success, sending Kafka event for friend request from {} to {}",
                event.getFromUserId(), event.getToUserId());
        BaseEvent<FriendRequestEvent> kafkaEvent = new BaseEvent<>();
        kafkaEvent.setType(EventType.FRIEND_REQUEST);
        kafkaEvent.setPayload(event);


        coreKafkaProducer.sendEvent(KafkaConstant.RELATION_OUT_EVENTS, event.getFromUserId(), kafkaEvent);
    }

}
