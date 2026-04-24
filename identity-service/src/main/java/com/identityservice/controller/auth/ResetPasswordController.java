package com.identityservice.controller.auth;


import com.commonlibrary.exception.AppException;
import com.identityservice.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class ResetPasswordController {

    private final AuthService authService;

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam String email, @RequestParam String otp, Model model) {
        model.addAttribute("email", email);
        model.addAttribute("otp", otp);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String email,
                                       @RequestParam String otp,
                                       @RequestParam String newPassword,
                                       @RequestParam String confirmPassword,
                                       Model model, RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp!");
            model.addAttribute("email", email);
            model.addAttribute("otp", otp);
            return "reset-password";
        }

        try {
            // Gọi hàm đổi mật khẩu trong Service của bạn
            authService.resetPassword(email, newPassword, otp);
            redirectAttributes.addFlashAttribute("success", "POWER RESTORED! Đã đổi mật khẩu thành công.");
            return "redirect:/login";
        } catch (AppException e) {
            model.addAttribute("error", e.getResultCode().name());
            return "reset-password";
        }
    }
}
