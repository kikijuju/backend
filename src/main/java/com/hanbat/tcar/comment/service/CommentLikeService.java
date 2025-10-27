package com.hanbat.tcar.comment.service;

import com.hanbat.tcar.comment.entity.Comment;
import com.hanbat.tcar.comment.entity.CommentLike;
import com.hanbat.tcar.comment.repository.CommentLikeRepository;
import com.hanbat.tcar.comment.repository.CommentRepository;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentLikeService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;

    public CommentLikeService(
            CommentRepository commentRepository,
            CommentLikeRepository commentLikeRepository,
            @Qualifier("customRedisTemplate") RedisTemplate<String, String> redisTemplate,
            UserRepository userRepository
    ) {
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }
    @Transactional
    public boolean toggleLike(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        return commentLikeRepository.findByUserAndComment(user, comment)
                .map(like -> {
                    commentLikeRepository.delete(like); // 좋아요 취소
                    return false;
                })
                .orElseGet(() -> {
                    CommentLike newLike = CommentLike.builder()
                            .user(user)
                            .comment(comment)
                            .build();
                    commentLikeRepository.save(newLike); // 좋아요 등록
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public int getLikeCount(Long commentId) {
        String key = "comment:likeCount:" + commentId;

        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Integer.parseInt(cached);
        }

        // 캐시에 없으면 DB 조회 + 캐싱
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        int count = commentLikeRepository.countByComment(comment);
        redisTemplate.opsForValue().set(key, String.valueOf(count));
        return count;
    }

    public boolean isLikedByUser(Long commentId, Long userId) {
        String key = "comment:liked:" + commentId + ":" + userId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return true;
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다."));

        boolean liked = commentLikeRepository.existsByUserAndComment(user, comment);
        if (liked) {
            redisTemplate.opsForValue().set(key, "1");
        }

        return liked;
    }
}