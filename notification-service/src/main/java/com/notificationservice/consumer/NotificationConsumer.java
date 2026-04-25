package com.notificationservice.consumer;


import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.UserEventDTO;
import com.commoncore.enums.otp.OtpType;
import com.notificationservice.dto.request.StateEmailRequest;
import com.notificationservice.enums.mail.StateSendMail;
import com.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final EmailService emailService;
    @jakarta.annotation.PostConstruct
    public void check() {
        log.info(">>>> ĐÃ KHỞI TẠO BEAN NOTIFICATION CONSUMER THÀNH CÔNG! <<<<");
    }
    @KafkaListener(
            topics = {KafkaConstant.USER_EVENTS_TOPIC},
            groupId = "notification-group"
    )
    public void handleEmailEvent(BaseEvent<UserEventDTO> event) {
        String type = event.getType();
        if (!isEmailEvent(type)) {
            log.debug("NotificationConsumer: Bỏ qua sự kiện không liên quan: {}", type);
            return;
        }

        UserEventDTO payload = event.getPayload();
        log.info("Kafka Consumer: Nhận sự kiện Email ID [{}] loại [{}]", event.getEventId(), type);

        StateSendMail state = determineEmailState(type);
        StateEmailRequest emailRequest = new StateEmailRequest(
                payload.getEmail(),
                payload.getCode(),
                state
        );

        try {
            emailService.sendStateEmail(emailRequest);
        } catch (Exception e) {
            log.error("Kafka Lỗi gửi mail: {}", e.getMessage());
        }
    }

    // Hàm lọc để code nhìn sạch hơn
    private boolean isEmailEvent(String type) {
        return "ACTIVATION".equalsIgnoreCase(type) || "RECOVERY".equalsIgnoreCase(type);
    }
    /**
     * Logic ánh xạ được tái sử dụng hoàn toàn
     */
    private StateSendMail determineEmailState(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return StateSendMail.REGISTER;
        }

        try {
            OtpType type = OtpType.valueOf(typeString.toUpperCase());
            return switch (type) {
                case RECOVERY -> StateSendMail.FORGOT_PASSWORD;
                case ACTIVATION -> StateSendMail.REGISTER;
            };
        } catch (IllegalArgumentException e) {
            log.warn("Không nhận diện được type: {}, dùng mặc định REGISTER", typeString);
            return StateSendMail.REGISTER;
        }
    }
}
