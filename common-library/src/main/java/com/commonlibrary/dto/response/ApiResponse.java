package com.commonlibrary.dto.response;

import com.commonlibrary.exception.ResultCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private int code;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Dùng mặc định cho case Success đơn giản (Thường Frontend chỉ check success=true và code=1000)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(ResultCode.SUCCESS.getCode())
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .code(ResultCode.SUCCESS.getCode())
                .message("Success")
                .build();
    }

    // Nếu muốn trả về Success kèm thông báo đa ngôn ngữ, Controller sẽ truyền message đã dịch vào đây
    public static <T> ApiResponse<T> success(ResultCode resultCode, String translatedMessage) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(resultCode.getCode())
                .message(translatedMessage)
                .data(null)
                .build();
    }

    // Các hàm Error bắt buộc phải truyền translatedMessage (chuỗi đã dịch) từ ExceptionHandler vào
    public static <T> ApiResponse<T> error(ResultCode errorCode, String translatedMessage) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(errorCode.getCode())
                .message(translatedMessage)
                .build();
    }
}