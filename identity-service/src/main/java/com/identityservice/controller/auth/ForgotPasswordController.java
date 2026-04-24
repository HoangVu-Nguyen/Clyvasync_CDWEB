package com.identityservice.controller.auth;


import com.commonlibrary.enums.otp.OtpType;
import com.commonlibrary.exception.AppException;
import com.identityservice.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final AuthService authService;

    // 1. Hiển thị trang Forgot Password
    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            // Gọi hàm forgotPassword trong Service (Hàm này sẽ sinh OTP và gửi mail)
            authService.forgotPassword(email);

            // Chuyển hướng sang trang verify-otp kèm theo email để user nhập mã
            redirectAttributes.addAttribute("email", email);
            redirectAttributes.addFlashAttribute("success", "RECOVERY CODE SENT! Kiểm tra hòm thư của bạn.");
            redirectAttributes.addAttribute("type", OtpType.RECOVERY.name());
            return "redirect:/verify-otp";

        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getResultCode().name());
            return "redirect:/forgot-password";
        }
    }
}
