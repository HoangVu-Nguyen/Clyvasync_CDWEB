package com.identityservice.controller.auth;


import com.commonlibrary.enums.otp.OtpType;
import com.commonlibrary.exception.AppException;
import com.identityservice.dto.request.RegisterRequest;
import com.identityservice.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RegisterController {

    // Inject Service chứa hàm register của bạn vào đây
    private final AuthService authService;

    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest request, Model model, RedirectAttributes redirectAttributes) {

        // 1. Validate Password khớp nhau (có thể để ở đây hoặc trong validateRegisterRequest của Service)
        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "register";
        }

        try {
            System.out.println(request.toString());
            // 2. Gọi hàm Service xịn xò của bạn
            authService.register(request);

            // 3. THÀNH CÔNG: Vì Service của bạn có sinh OTP, nên chúng ta không ở lại trang Đăng ký nữa.
            // Hãy Redirect người dùng sang trang nhập mã OTP.
            // Truyền theo email lên URL để trang OTP biết đang xác thực cho ai
            redirectAttributes.addAttribute("email", request.getEmail());
            redirectAttributes.addAttribute("type", OtpType.ACTIVATION.name());
            return "redirect:/verify-otp";

        } catch (AppException e) {
            // 4. BẮT LỖI NGHIỆP VỤ (Từ ResultCode của bạn)
            // Ví dụ: USER_EXISTED, PLEASE_WAIT_BEFORE_RESENDING,...
            log.warn("Lỗi đăng ký: {}", e.getResultCode().name());
            model.addAttribute("error", e.getResultCode().name());
            return "register"; // Trả lại form kèm câu thông báo lỗi chuẩn xác

        } catch (Exception e) {
            // 5. BẮT LỖI HỆ THỐNG (Database chết, Redis lỗi...)
            log.error("Lỗi hệ thống khi đăng ký", e);
            model.addAttribute("error", "Hệ thống đang bận, vui lòng thử lại sau!");
            return "register";
        }
    }
}