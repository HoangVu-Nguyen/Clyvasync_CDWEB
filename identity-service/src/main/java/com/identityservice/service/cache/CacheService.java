package com.identityservice.service.cache;



import com.identityservice.enums.cache.RedisKeyType;

import java.time.Duration;

public interface CacheService {
    void save(String key, Object value, Duration ttl);
    Object get(String key);
    void delete(String key);

    void saveOtp(String email, String otp, RedisKeyType type);
    String getOtp(String email, RedisKeyType type);
    boolean hasKey(String email, RedisKeyType type);
    boolean isSpamming(String email, RedisKeyType type);
    void setProcessLimit(String email, RedisKeyType type);
     void increaseFailedAttempts(String email);
     boolean isBruteForce(String email);
     void resetFailedAttempts(String email);
     void lockAccount(String email) ;

     boolean isAccountLocked(String email) ;
    long increment(String key, long timeoutInSeconds);
    boolean hasKey(String key);

}
