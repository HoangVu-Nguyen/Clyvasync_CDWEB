package com.commoncore.exception;



import com.commoncore.dto.response.ApiResponse;
import com.commoncore.utils.Translator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Translator translator;

    /**
     * Bắt lỗi từ Validation ở DTO (ví dụ: @NotBlank(message = "EMAIL_REQUIRED"))
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getBindingResult().getFieldError()).getDefaultMessage();

        ResultCode resultCode = ResultCode.INVALID_KEY;

        try {
            if (enumKey != null) {
                resultCode = ResultCode.valueOf(enumKey);
            }
        } catch (IllegalArgumentException e) {
            // Khi nhảy vào đây tức là cấu hình message="XXX" ở DTO nhưng quên tạo Enum XXX.
            log.warn("Lỗi Validation: Không tìm thấy Enum nào tên là '{}'", enumKey);
        }

        return ResponseEntity
                .status(resultCode.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(resultCode.getCode())
                        // MA THUẬT NẰM Ở DÒNG NÀY: Dịch mã Enum thành câu chữ tùy theo ngôn ngữ Client
                        .message(translator.toLocale(resultCode))
                        .build());
    }

    /**
     * Bắt lỗi chủ động từ Logic (ví dụ: throw new AppException(ResultCode.USER_NOT_FOUND))
     */
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Void>> handlingAppException(AppException exception) {
        ResultCode resultCode = exception.getResultCode();

        return ResponseEntity
                .status(resultCode.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(resultCode.getCode())
                        // VÀ Ở ĐÂY NỮA
                        .message(translator.toLocale(resultCode))
                        .build());
    }

    /**
     * Bắt tất cả các lỗi còn lọt lưới (Lỗi 500)
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiResponse<Void>> handlingGeneralException(Exception exception) {
        log.error("Lỗi hệ thống không xác định: ", exception);

        ResultCode resultCode = ResultCode.UNCATEGORIZED_EXCEPTION;

        return ResponseEntity
                .status(resultCode.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(resultCode.getCode())
                        .message(translator.toLocale(resultCode))
                        .build());
    }
}