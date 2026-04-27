package com.notificationservice.consumer;


import com.commoncore.contanst.KafkaConstant;
import com.commoncore.dto.event.BaseEvent;
import com.commoncore.dto.event.UserEvent;
import com.commoncore.enums.event.EventType;
import com.notificationservice.dto.request.StateEmailRequest;
import com.notificationservice.enums.mail.StateSendMail;
import com.notificationservice.service.EmailService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {
    private final EmailService emailService;
    @PostConstruct
    public void check() {
        log.info(">>>> ĐÃ KHỞI TẠO BEAN NOTIFICATION CONSUMER THÀNH CÔNG! <<<<");
    }
    @KafkaListener(
            topics = KafkaConstant.USER_EVENTS_TOPIC,
            groupId = "notification-mail"
    )
    public void handleEmailEvent(BaseEvent<UserEvent> event) {
        log.info(">>>> JSON THÔ NHẬN ĐƯỢC: {}", event);
        EventType type = event.getType();
        System.out.println(type);
        if (!isEmailEvent(type)) {
            log.debug("NotificationConsumer: Bỏ qua sự kiện không liên quan: {}", type);
            return;
        }

        UserEvent payload = event.getPayload();
        log.info("Kafka Consumer: Nhận sự kiện Email ID [{}] loại [{}]", event.getEventId(), type);

        StateSendMail state = determineEmailState(type.name());
        StateEmailRequest emailRequest = new StateEmailRequest(
                payload.getEmail(),
                payload.getCode(),
                state
        );

        switch (event.getType()) {
            case REGISTER_OTP:
                log.info("Xử lý gửi mail đăng ký cho: {}", payload.getEmail());
                emailService.sendStateEmail(emailRequest);
                break;

            case FORGOT_PASSWORD:
                log.info("Xử lý gửi mail quên mật khẩu cho: {}", payload.getEmail());
                break;

            default:
                log.debug("Sự kiện {} không cần gửi email, bỏ qua.", event.getType());
        }
    }

    // Hàm lọc để code nhìn sạch hơn
    private boolean isEmailEvent(EventType type) {
        return type == EventType.USER_REGISTERED
                || type == EventType.FORGOT_PASSWORD
                || type == EventType.REGISTER_OTP;
    }
    /**
     * Logic ánh xạ được tái sử dụng hoàn toàn
     */
    private StateSendMail determineEmailState(String typeString) {
        if (typeString == null || typeString.trim().isEmpty()) {
            return StateSendMail.REGISTER;
        }

        try {
            EventType type = EventType.valueOf(typeString.toUpperCase());
            return switch (type) {
                case REGISTER_OTP -> StateSendMail.REGISTER;
                case FORGOT_PASSWORD -> StateSendMail.FORGOT_PASSWORD;
                case USER_REGISTERED -> StateSendMail.REGISTER;
                case USER_UPDATE -> null;
                case AVATAR_UPDATED -> null;
                case COVER_UPDATED -> null;
            };
        } catch (IllegalArgumentException e) {
            log.warn("Không nhận diện được type: {}, dùng mặc định REGISTER", typeString);
            return StateSendMail.REGISTER;
        }
    }

}
