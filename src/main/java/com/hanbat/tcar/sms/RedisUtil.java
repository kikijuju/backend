package com.hanbat.tcar.sms;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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

    /**
     * key로부터 value를 조회
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * key 삭제
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
