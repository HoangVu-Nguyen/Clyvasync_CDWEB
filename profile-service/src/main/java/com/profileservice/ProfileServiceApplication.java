package com.profileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.profileservice",
        "com.commonlibrary",
        "com.commoncore"
})
@Import(com.commonsecurity.config.SecurityConfig.class)
public class ProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }

}
