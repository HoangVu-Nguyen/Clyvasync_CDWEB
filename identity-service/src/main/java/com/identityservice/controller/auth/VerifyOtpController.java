package com.identityservice.controller.auth;


import com.commonlibrary.enums.otp.OtpType;
import com.commonlibrary.exception.AppException;
import com.identityservice.dto.request.ResendVerificationRequest;
import com.identityservice.dto.request.VerifyAccountRequest;
import com.identityservice.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VerifyOtpController {
    private final AuthService authService;

    @GetMapping("/verify-otp")
    public String showVerifyPage(@RequestParam String email,
                                 @RequestParam(defaultValue = "ACTIVATION") OtpType type, // Đổi thành Enum
                                 Model model) {
        model.addAttribute("email", email);
        model.addAttribute("type", type.name()); // Trả về dạng String cho HTML dễ dùng
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processOtp(@RequestParam String email,
                             @RequestParam String code,
                             @RequestParam OtpType type, // Hứng Enum
                             Model model, RedirectAttributes redirectAttributes) {
        try {
            // Thay vì equals string, giờ xài thẳng Enum
            if (type == OtpType.RECOVERY) {
                authService.verifyPasswordResetOtp(code, email);
                redirectAttributes.addAttribute("email", email);
                redirectAttributes.addAttribute("otp", code);
                return "redirect:/reset-password";
            } else {
                VerifyAccountRequest request = new VerifyAccountRequest();
                request.setEmail(email);
                request.setCode(code);
                authService.verifyAccount(request);
                redirectAttributes.addFlashAttribute("success", "STAGE CLEARED! Vui lòng đăng nhập.");
                return "redirect:/login";
            }
        } catch (AppException e) {
            model.addAttribute("error", e.getResultCode().name());
            model.addAttribute("email", email);
            model.addAttribute("type", type.name());
            return "verify-otp";
        }
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email,
                            @RequestParam(defaultValue = "ACTIVATION") OtpType type,
                            RedirectAttributes redirectAttributes) {
        try {
            ResendVerificationRequest request = new ResendVerificationRequest();
            request.setEmail(email);
            request.setType(type);
            authService.resendVerification(request);
            redirectAttributes.addFlashAttribute("success", "1UP! Đã gửi mã bí mật mới vào hòm thư.");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getResultCode().name());
        }
        redirectAttributes.addAttribute("email", email);
        redirectAttributes.addAttribute("type", type.name());
        return "redirect:/verify-otp";
    }
}