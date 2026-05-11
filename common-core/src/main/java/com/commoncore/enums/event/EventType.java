package com.commoncore.enums.event;


import lombok.Getter;

@Getter
public enum EventType {
    // Luồng Authentication & Identity
    REGISTER_OTP("Gửi mã OTP đăng ký"),
    FORGOT_PASSWORD("Yêu cầu quên mật khẩu"),
    USER_REGISTERED("Người dùng đã đăng ký thành công"),
    USER_UPDATE("Cập nhận thông tin người dùng")
    ,

    // Luồng Media & Profile
    AVATAR_UPDATED("Cập nhật ảnh đại diện"),
    FRIEND_REQUEST("Gửi lời mời kết bạn "),
    ACCEPT_FRIEND("Đồng ý kết bạn"),
    LIKE_POST("Đã thích bài viết"),
    COVER_UPDATED("Cập nhật ảnh bìa");

    private final String description;

    EventType(String description) {
        this.description = description;
    }
}