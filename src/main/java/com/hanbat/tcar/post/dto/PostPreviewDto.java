package com.hanbat.tcar.post.dto;

import com.hanbat.tcar.post.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostPreviewDto {
    private Long id;
    private String title;
    private String authorNickname;
    private int viewCount;
    private int likeCount;
    private LocalDateTime createdAt;

    public static PostPreviewDto from(Post post, int viewCount, int likeCount) {
        return PostPreviewDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .authorNickname(post.getAuthor().getNickname())
                .viewCount(viewCount)
                .likeCount(likeCount)
                .createdAt(post.getCreatedAt())
                .build();
    }
}
