package com.hanbat.tcar.sms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    /**
     * key-value를 저장하고, 특정 시간(분) 뒤에 만료되도록 설정
     */
    public void set(String key, String value, long minutes) {
        redisTemplate.opsForValue().set(key, value, minutes, TimeUnit.MINUTES);
    }

    public void set(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    /**
     * key로부터 value를 조회
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // value가 null / 빈 문자열이 아닌지
    public boolean checkExistsValue(String value) {
        return value != null && !value.isEmpty();
    }

    /**
     * key 삭제
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
