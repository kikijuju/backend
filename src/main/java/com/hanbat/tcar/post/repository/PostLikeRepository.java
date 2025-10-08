package com.hanbat.tcar.post.repository;

import com.hanbat.tcar.post.entity.Post;
import com.hanbat.tcar.post.entity.PostLike;
import com.hanbat.tcar.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    int countByPost(Post post);
    Optional<PostLike> findByUserAndPost(User user, Post post);
}