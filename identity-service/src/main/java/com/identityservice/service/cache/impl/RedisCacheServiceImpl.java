package com.identityservice.service.cache.impl;


import com.identityservice.enums.cache.RedisKeyType;
import com.identityservice.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void saveOtp(String email, String otp, RedisKeyType type) {
        String finalKey = type.name() + ":" + email;
        this.save(finalKey, otp, Duration.ofMinutes(15));
        log.info("Đã lưu OTP vào Redis cho email: {} với key: {}", email, finalKey);
    }

    @Override
    public String getOtp(String email, RedisKeyType type) {
        String finalKey = type.name() + ":" + email;
        return (String) this.get(finalKey);
    }

    @Override
    public boolean hasKey(String email, RedisKeyType type) {
        return redisTemplate.hasKey(type.name() + ":" + email);
    }
    @Override
    public boolean isSpamming(String email, RedisKeyType type) {
        return redisTemplate.hasKey(type.getFullKey(email));
    }

    @Override
    public void setProcessLimit(String email, RedisKeyType type) {
        String key = type.getFullKey(email);
        redisTemplate.opsForValue().set(key, "blocked", type.getDefaultTtl(), type.getTimeUnit());
    }
    @Override
    public void increaseFailedAttempts(String email) {
        String key = RedisKeyType.FAILED_ATTEMPTS.getFullKey(email);
        Long attempts = redisTemplate.opsForValue().increment(key);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, RedisKeyType.FAILED_ATTEMPTS.getDefaultTtl(), RedisKeyType.FAILED_ATTEMPTS.getTimeUnit());
        }

        if (attempts != null && attempts >= 10) {
            log.warn("Tài khoản {} đã nhập sai {} lần. Tiến hành khóa 24h.", email, attempts);
            this.lockAccount(email);


            this.resetFailedAttempts(email);
        }
    }

    @Override
    public boolean isBruteForce(String email) {
        String key = RedisKeyType.FAILED_ATTEMPTS.getFullKey(email);
        Object val = redisTemplate.opsForValue().get(key);

        if (val == null) return false;

        try {
            int attempts = Integer.parseInt(val.toString());
            return attempts >= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void resetFailedAttempts(String email) {
        redisTemplate.delete(RedisKeyType.FAILED_ATTEMPTS.getFullKey(email));
    }
    @Override
    public void lockAccount(String email) {
        String key = RedisKeyType.BLOCK_LOGIN.getFullKey(email);
        redisTemplate.opsForValue().set(key, "locked", 24, TimeUnit.HOURS);
    }

    @Override
    public boolean isAccountLocked(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyType.BLOCK_LOGIN.getFullKey(email)));
    }
    @Override
    public long increment(String key, long timeoutInSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, timeoutInSeconds, TimeUnit.SECONDS);
        }
        return count != null ? count : 0;
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

}
