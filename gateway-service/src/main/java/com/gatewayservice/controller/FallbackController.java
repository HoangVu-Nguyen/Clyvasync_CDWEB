package com.gatewayservice.controller;


import com.commoncore.dto.response.ApiResponse;
import com.commoncore.exception.ResultCode;
import com.commoncore.utils.Translator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/fallback/identity")
    public ApiResponse<Map<String, String>> identityFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Service Unavailable");
        response.put("message", "Hệ thống xác thực hiện đang quá tải hoặc tạm thời gián đoạn. Vui lòng thử lại sau.");

        return ApiResponse.error(ResultCode.FAILED,response, Translator.class.getName());
    }
}