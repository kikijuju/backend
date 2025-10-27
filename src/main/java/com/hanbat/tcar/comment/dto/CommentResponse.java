package com.hanbat.tcar.comment.dto;

import com.hanbat.tcar.comment.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {
    private Long id;
    private String authorNickname;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int likeCount;
    private boolean isLiked;

    public static CommentResponse from(Comment comment, int likeCount, boolean isLiked) {
        return CommentResponse.builder()
                .id(comment.getId())
                .authorNickname(comment.getAuthor().getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .likeCount(likeCount)
                .isLiked(isLiked)
                .build();
    }
}