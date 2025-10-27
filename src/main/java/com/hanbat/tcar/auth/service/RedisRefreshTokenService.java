package com.hanbat.tcar.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisRefreshTokenService implements RefreshTokenService {

    private static final String RT_PREFIX = "RT:";        // RT:{refresh} -> userId
    private static final String UID_PREFIX = "USER_ID:";  // USER_ID:{uid} -> refresh

    private final StringRedisTemplate redis;

    @Override
    public void storeRefreshToken(Long userId, String refreshToken, long daysTtl) {
        final String rtKey = RT_PREFIX + refreshToken;
        final String uidKey = UID_PREFIX + userId;
        final Duration ttl = Duration.ofDays(daysTtl);

        // 기존 사용자 토큰이 있다면 먼저 정리(중복 세션을 허용하지 않겠다면)
        String prev = redis.opsForValue().get(uidKey);
        if (prev != null && !prev.equals(refreshToken)) {
            // 이전 매핑 제거
            redis.delete(RT_PREFIX + prev);
        }

        // 1) RT:{refresh} -> userId (TTL 함께 설정)
        redis.opsForValue().set(rtKey, String.valueOf(userId), ttl);

        // 2) USER_ID:{uid} -> refresh (TTL 함께 설정)
        redis.opsForValue().set(uidKey, refreshToken, ttl);
    }

    @Override
    public Long getUserIdByRefreshToken(String refreshToken) {
        String val = redis.opsForValue().get(RT_PREFIX + refreshToken);
        return (val == null) ? null : Long.valueOf(val);
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        final String rtKey = RT_PREFIX + refreshToken;
        String uidStr = redis.opsForValue().get(rtKey);
        if (uidStr != null) {
            String uidKey = UID_PREFIX + uidStr;
            // USER_ID가 가리키는 토큰이 동일할 때만 정리 (다른 세션을 실수로 지우지 않도록)
            String cur = redis.opsForValue().get(uidKey);
            if (refreshToken.equals(cur)) {
                redis.delete(uidKey);
            }
        }
        redis.delete(rtKey);
    }

    @Override
    public void deleteRefreshTokenForUser(Long userId) {
        final String uidKey = UID_PREFIX + userId;
        String refreshToken = redis.opsForValue().get(uidKey);
        if (refreshToken != null) {
            redis.delete(RT_PREFIX + refreshToken);
            redis.delete(uidKey);
        }
    }

    @Override
    public long getTtlSeconds(String refreshToken) {
        Long sec = redis.getExpire(RT_PREFIX + refreshToken, TimeUnit.SECONDS);
        return (sec == null) ? -2 : sec; // -2: 키 없음, -1: TTL 없음
    }

    @Override
    public void rotateRefreshToken(Long userId, String oldRefreshToken, String newRefreshToken, long daysTtl) {
        // 1) 기존 토큰 제거(사용자/토큰 양쪽 키 모두)
        if (oldRefreshToken != null && !oldRefreshToken.isBlank()) {
            deleteRefreshToken(oldRefreshToken);
        }
        // 2) 새 토큰 저장
        storeRefreshToken(userId, newRefreshToken, daysTtl);
    }
}
