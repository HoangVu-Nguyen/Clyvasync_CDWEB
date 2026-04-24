package com.notificationservice.service.impl;


import com.notificationservice.dto.request.StateEmailRequest;
import com.notificationservice.enums.mail.StateSendMail;
import com.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendStateEmail(StateEmailRequest request) {
        try {
            log.info("Đang chuẩn bị gửi email [{}] tới: {}", request.getState().getTitle(), request.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            // Sử dụng UTF-8 để không bị lỗi font tiếng Việt
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // 1. Đổ dữ liệu vào context của Thymeleaf
            Context context = new Context();
            context.setVariable("email", request.getEmail());
            context.setVariable("code", request.getCode());
            context.setVariable("title", request.getState().getTitle());
            context.setVariable("intro", request.getState().getIntro());

            // 2. Render ra file HTML (File này nằm ở: src/main/resources/templates/email/state-mail.html)
            String htmlContent = templateEngine.process("email/state-mail", context);

            // 3. Thiết lập thông tin người nhận và nội dung
            helper.setTo(request.getEmail());
            helper.setSubject("Clyvasync - " + request.getState().getTitle());
            helper.setText(htmlContent, true); // true để gửi định dạng HTML
            helper.setFrom("Clyvasync Support <no-reply@clyvasync.com>");

            // 4. Bắn mail đi
            mailSender.send(message);
            log.info("Email [{}] đã gửi thành công tới {}", request.getState().getTitle(), request.getEmail());

        } catch (MessagingException e) {
            log.error("Lỗi MessagingException khi gửi mail tới {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Gửi mail thất bại do lỗi hệ thống thư thoại", e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gửi mail: {}", e.getMessage());
            throw new RuntimeException("Gửi mail thất bại", e);
        }
    }
}