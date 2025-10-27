// 📦 게시글 조회수 Redis → DB 동기화 스케줄러
package com.hanbat.tcar.post.scheduler;

import com.hanbat.tcar.post.entity.Post;
import com.hanbat.tcar.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewCountSyncScheduler {

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;

    private static final String VIEW_COUNT_PREFIX = "post:viewCount:";

     // 5분마다 Redis에 저장된 조회수를 DB로 반영
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5분
    public void syncViewCountsToDatabase() {
        Set<String> keys = redisTemplate.keys(VIEW_COUNT_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                Long postId = Long.valueOf(key.replace(VIEW_COUNT_PREFIX, ""));
                String countStr = redisTemplate.opsForValue().get(key);
                if (countStr == null) continue;

                int redisCount = Integer.parseInt(countStr);

                Post post = postRepository.findById(postId)
                        .orElse(null);
                if (post == null) continue;

                post.increaseViewCount(redisCount);
                postRepository.save(post);

                redisTemplate.delete(key); // 캐시 초기화

                log.info("[조회수 동기화] postId={} → {}회", postId, redisCount);

            } catch (Exception e) {
                log.warn("[조회수 동기화 실패] key={}, reason={}", key, e.getMessage());
            }
        }
    }
}
