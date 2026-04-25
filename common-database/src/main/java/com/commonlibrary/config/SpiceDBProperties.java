package com.commonlibrary.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.spicedb")
public class SpiceDBProperties {
    private String host = "localhost";
    private int port = 50051;
    private String token = "65906590";
    private boolean useSsl = false;
}