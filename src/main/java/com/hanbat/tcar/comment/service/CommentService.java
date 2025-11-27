// 📦 Redis 기반 댓글 좋아요 캐싱 적용
package com.hanbat.tcar.comment.service;

import com.hanbat.tcar.comment.dto.CommentCreateRequest;
import com.hanbat.tcar.comment.dto.CommentResponse;
import com.hanbat.tcar.comment.dto.CommentUpdateRequest;
import com.hanbat.tcar.comment.entity.Comment;
import com.hanbat.tcar.comment.entity.CommentLike;
import com.hanbat.tcar.comment.repository.CommentLikeRepository;
import com.hanbat.tcar.comment.repository.CommentRepository;
import com.hanbat.tcar.post.entity.Post;
import com.hanbat.tcar.post.repository.PostRepository;
import com.hanbat.tcar.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final StringRedisTemplate redisTemplate;

    private static final Duration TTL = Duration.ofHours(1);

    @Transactional
    public Long writeComment(Long postId, CommentCreateRequest request, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .author(user)
                .post(post)
                .content(request.getContent()) // DTO에서 꺼내기
                .build();

        return commentRepository.save(comment).getId();
    }

    @Transactional
    public boolean toggleLike(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        String likeCountKey = "comment:likeCount:" + commentId;
        String isLikedKey = "comment:liked:" + commentId + ":" + user.getId();

        return commentLikeRepository.findByUserAndComment(user, comment)
                .map(like -> {
                    commentLikeRepository.delete(like);
                    redisTemplate.delete(isLikedKey);
                    redisTemplate.opsForValue().decrement(likeCountKey);
                    return false;
                })
                .orElseGet(() -> {
                    CommentLike newLike = CommentLike.builder()
                            .user(user)
                            .comment(comment)
                            .build();
                    commentLikeRepository.save(newLike);
                    redisTemplate.opsForValue().set(isLikedKey, "1", TTL);
                    redisTemplate.opsForValue().increment(likeCountKey);
                    redisTemplate.expire(likeCountKey, TTL);
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        List<Comment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post);

        return comments.stream()
                .map(comment -> {
                    String likeCountKey = "comment:likeCount:" + comment.getId();
                    String isLikedKey = "comment:liked:" + comment.getId() + ":" + user.getId();

                    // 좋아요 수 캐시 조회 또는 fallback
                    String cachedCount = redisTemplate.opsForValue().get(likeCountKey);
                    int likeCount = (cachedCount != null)
                            ? Integer.parseInt(cachedCount)
                            : commentLikeRepository.countByComment(comment);

                    // 좋아요 여부
                    boolean isLiked = redisTemplate.hasKey(isLikedKey)
                            || commentLikeRepository.existsByUserAndComment(user, comment);

                    return CommentResponse.from(comment, likeCount, isLiked);
                })
                .toList();
    }

    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequest request, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("본인만 수정할 수 있습니다.");
        }

        comment.update(request.getContent());
    }

    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글이 존재하지 않습니다."));

        if (!comment.getAuthor().getId().equals(user.getId()) &&
                !user.getTier().name().equals("ADMIN")) {
            throw new SecurityException("본인 또는 관리자만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}