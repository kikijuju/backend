package com.hanbat.tcar.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    // 변경: RedisTemplate → StringRedisTemplate
    private final StringRedisTemplate redisTemplate;

    public void storeRefreshToken(Long userId, String refreshToken, long daysToExpire) {
        String refreshKey = "RT:" + refreshToken;
        String userKey = "USER_ID:" + userId;

        // 1. RT:{refreshToken} -> userId
        redisTemplate.opsForValue().set(refreshKey, String.valueOf(userId));
        redisTemplate.expire(refreshKey, Duration.ofDays(daysToExpire));

        // 2. USER_ID:{userId} -> refreshToken
        redisTemplate.opsForValue().set(userKey, refreshToken);
        redisTemplate.expire(userKey, Duration.ofDays(daysToExpire));
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

    //
    public void deleteRefreshTokenForUser(Long userId) {
        String refreshToken = redisTemplate.opsForValue().get("USER_ID:" + userId);
        if (refreshToken != null) {
            redisTemplate.delete("RT:" + refreshToken);
            redisTemplate.delete("USER_ID:" + userId);
        }
    }
}
