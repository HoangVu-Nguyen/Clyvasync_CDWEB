package com.identityservice.enums.cache;


import lombok.Getter;

import java.util.concurrent.TimeUnit;

@Getter
public enum RedisKeyType {
    VERIFY_ACCOUNT("auth:verify:", 15, TimeUnit.MINUTES),
    RESET_PASSWORD("auth:reset:", 10, TimeUnit.MINUTES),
    USER_PROFILE("user:profile:", 60, TimeUnit.MINUTES),
    LOGIN_ATTEMPT("auth:login_attempt:", 5, TimeUnit.MINUTES),
    SEND_EMAIL_LIMIT("auth:limit:", 60, TimeUnit.SECONDS),
    FAILED_ATTEMPTS("auth:failed_attempts:", 1, TimeUnit.HOURS),
    RATE_LIMIT("auth:rate_limit:", 10, TimeUnit.MINUTES),
    BLOCK_LOGIN("auth:block:", 24, TimeUnit.HOURS), // Nếu sai quá nhiều thì khóa 24,
    REFRESH_TOKEN("auth:refresh_token:", 7, TimeUnit.DAYS);

    private final String prefix;
    private final long defaultTtl;
    private final TimeUnit timeUnit;

    RedisKeyType(String prefix, long defaultTtl, TimeUnit timeUnit) {
        this.prefix = prefix;
        this.defaultTtl = defaultTtl;
        this.timeUnit = timeUnit;
    }

    public String getFullKey(String suffix) {
        return this.prefix + suffix;
    }
}