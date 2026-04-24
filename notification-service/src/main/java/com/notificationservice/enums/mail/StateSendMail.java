package com.notificationservice.enums.mail;

import lombok.Getter;

@Getter
public enum StateSendMail {
    // Định nghĩa các trạng thái Email kèm theo Tiêu đề và Lời dẫn tương ứng
    REGISTER(
            "Xác thực đăng ký tài khoản",
            "Cảm ơn bạn đã lựa chọn Clyvasync. Vui lòng sử dụng mã OTP dưới đây để hoàn tất quá trình đăng ký tài khoản của bạn."
    ),

    FORGOT_PASSWORD(
            "Khôi phục mật khẩu",
            "Chúng tôi nhận được yêu cầu thay đổi mật khẩu cho tài khoản của bạn. Vui lòng nhập mã xác nhận sau để tiếp tục."
    ),

    CHANGE_EMAIL(
            "Thay đổi Email liên kết",
            "Bạn đang thực hiện thay đổi Email cho tài khoản. Đây là mã xác nhận để đảm bảo tính bảo mật."
    ),

    TWO_FACTOR_AUTH(
            "Mã xác thực 2 lớp (2FA)",
            "Phát hiện đăng nhập từ thiết bị lạ. Vui lòng nhập mã này để xác minh danh tính của bạn."
    );

    private final String title;
    private final String intro;

    StateSendMail(String title, String intro) {
        this.title = title;
        this.intro = intro;
    }
}