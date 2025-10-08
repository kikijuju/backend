package com.hanbat.tcar.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentUpdateRequest {
    @NotBlank
    private String content;
}