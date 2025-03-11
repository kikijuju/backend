package com.hanbat.tcar.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    // 변경: RedisTemplate → StringRedisTemplate
    private final StringRedisTemplate redisTemplate;

    public void storeRefreshToken(Long userId, String refreshToken, long daysToExpire) {
        String key = "RT:" + refreshToken;
        redisTemplate.opsForValue().set(key, String.valueOf(userId));
        redisTemplate.expire(key, Duration.ofDays(daysToExpire));
    }

    public Long getUserIdByRefreshToken(String refreshToken) {
        String key = "RT:" + refreshToken;
        String val = redisTemplate.opsForValue().get(key);
        return (val == null) ? null : Long.valueOf(val);
    }

    public void deleteRefreshToken(String refreshToken) {
        String key = "RT:" + refreshToken;
        redisTemplate.delete(key);
    }
}
