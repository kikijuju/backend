package com.hanbat.tcar.post.dto;

import com.hanbat.tcar.post.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class PostUpdateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private PostCategory category;

    private boolean isPublic;
}