package com.hanbat.tcar.comment.repository;

import com.hanbat.tcar.comment.entity.Comment;
import com.hanbat.tcar.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(Post post);
    int countByPost(Post post);
}