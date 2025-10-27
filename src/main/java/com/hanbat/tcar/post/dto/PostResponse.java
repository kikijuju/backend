// 게시글 응답 DTO
package com.hanbat.tcar.post.dto;

import com.hanbat.tcar.post.entity.Post;
import com.hanbat.tcar.post.entity.PostCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private String authorNickname;
    private PostCategory category;
    private boolean isPublic;
    private boolean isPinned; // 🔐 상단 고정 여부 추가
    private int viewCount;
    private int commentCount;
    private int likeCount;
    private boolean isLiked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 상세 조회용: 좋아요 여부 포함
    public static PostResponse from(Post post, int viewCount, int commentCount, int likeCount, boolean isLiked) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname())
                .category(post.getCategory())
                .isPublic(post.isPublic())
                .isPinned(post.isPinned())
                .viewCount(viewCount)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isLiked(isLiked)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // 목록 조회용: 좋아요 여부는 false로 고정
    public static PostResponse from(Post post, int viewCount, int commentCount, int likeCount) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname())
                .category(post.getCategory())
                .isPublic(post.isPublic())
                .isPinned(post.isPinned())
                .viewCount(viewCount)
                .commentCount(commentCount)
                .likeCount(likeCount)
                .isLiked(false)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    // (기본 fallback 용도)
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .authorNickname(post.getAuthor().getNickname())
                .category(post.getCategory())
                .isPublic(post.isPublic())
                .isPinned(post.isPinned())
                .viewCount(post.getViewCount().intValue())
                .commentCount(0)
                .likeCount(0)
                .isLiked(false)
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
