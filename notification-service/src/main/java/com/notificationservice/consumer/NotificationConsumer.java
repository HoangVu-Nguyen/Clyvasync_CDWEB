package com.notificationservice.consumer;


import com.commonlibrary.contanst.KafkaConstant;
import com.commonlibrary.dto.event.UserEventDTO;
import com.commonlibrary.enums.otp.OtpType;
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
            topics = {
                    KafkaConstant.USER_REGISTERED,
                    KafkaConstant.USER_ACTIVATED,
                    KafkaConstant.FORGOT_PASSWORD
            },
            groupId = "notification-group"
    )
    public void handleEmailEvent(UserEventDTO payload) {
        log.info("Kafka Consumer: Nhận được sự kiện [{}] từ Identity Service cho {}",
                payload.getType(), payload.getEmail());

        // 1. Logic ánh xạ giữ nguyên (Vì đây là logic nghiệp vụ của ông)
        StateSendMail state = determineEmailState(payload.getType());

        // 2. Chuyển đổi Request
        StateEmailRequest emailRequest = new StateEmailRequest(
                payload.getEmail(),
                payload.getCode(),
                state
        );

        // 3. Thực hiện gửi mail qua MailService
        try {
            emailService.sendStateEmail(emailRequest);
            log.info("Kafka: Đã xử lý gửi mail {} thành công cho: {}", state.name(), payload.getEmail());
        } catch (Exception e) {
            log.error("Kafka: Lỗi gửi mail cho {}: {}", payload.getEmail(), e.getMessage());
            // Kafka không tự động retry như RabbitMQ nếu ông không cấu hình
            // DefaultErrorHandler hoặc RetryTemplate. Tạm thời cứ throw để log lỗi.
            throw e;
        }
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
