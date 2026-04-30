package com.commoncore.resolver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaUrlResolver {

    @Value("${app.cdn.url:https://cdn.clyvasync.com}")
    private String cdnUrl;


    public String resolve(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        if (objectKey.startsWith("http")) {
            return objectKey;
        }

        String normalizedCdnUrl = cdnUrl.endsWith("/")
                ? cdnUrl.substring(0, cdnUrl.length() - 1)
                : cdnUrl;

        String normalizedKey = objectKey.startsWith("/")
                ? objectKey
                : "/" + objectKey;

        return normalizedCdnUrl + normalizedKey;
    }
}