// 📦 게시글 좋아요 토글 + 좋아요 여부 확인 서비스
package com.hanbat.tcar.post.service;

import com.hanbat.tcar.post.entity.Post;
import com.hanbat.tcar.post.entity.PostLike;
import com.hanbat.tcar.post.repository.PostLikeRepository;
import com.hanbat.tcar.post.repository.PostRepository;
import com.hanbat.tcar.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    @Transactional
    public boolean toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        return postLikeRepository.findByUserAndPost(user, post)
                .map(like -> {
                    postLikeRepository.delete(like); // 좋아요 취소
                    return false;
                })
                .orElseGet(() -> {
                    PostLike newLike = PostLike.builder()
                            .user(user)
                            .post(post)
                            .build();
                    postLikeRepository.save(newLike); // 좋아요 등록
                    return true;
                });
    }

    @Transactional(readOnly = true)
    public int getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));
        return postLikeRepository.countByPost(post);
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long postId, User user) {
        if (user == null) return false; // 비로그인 사용자는 false

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        return postLikeRepository.findByUserAndPost(user, post).isPresent();
    }
}