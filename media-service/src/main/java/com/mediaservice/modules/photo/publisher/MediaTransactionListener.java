package com.mediaservice.modules.photo.publisher;

import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.MediaUpdateEvent;
import com.commoncore.producer.CoreKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaTransactionListener {

    private final CoreKafkaProducer coreKafkaProducer;

    // Chỉ kích hoạt khi DB đã COMMIT 100% thành công
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMediaUpdateCommit(MediaUpdateEvent event) {
        log.info("Transaction lưu ảnh đã Commit an toàn. Chuyển lệnh cho Kafka Producer xử lý: {}", event.userId());

        coreKafkaProducer.sendEvent(KafkaConstant.MEDIA_UPDATE_TOPIC, event.userId(), event);
    }
}