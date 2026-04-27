package com.identityservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
        "com.identityservice", // Thay bằng package gốc của service ông (ví dụ: com.clyvasync.identity)
        "com.commonlibrary" ,   // ĐÂY LÀ CHỖ QUAN TRỌNG: Package chứa MyMetaObjectHandler,
        "com.commoncore"
})@EnableDiscoveryClient
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }

}
