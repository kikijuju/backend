package com.hanbat.tcar.comment.repository;

import com.hanbat.tcar.comment.entity.Comment;
import com.hanbat.tcar.comment.entity.CommentLike;
import com.hanbat.tcar.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    boolean existsByUserAndComment(User user, Comment comment);

    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    int countByComment(Comment comment);
}