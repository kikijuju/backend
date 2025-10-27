package com.hanbat.tcar.comment.controller;

import com.hanbat.tcar.comment.service.CommentLikeService;
import com.hanbat.tcar.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments/{commentId}/like")
@Tag(name = "댓글 좋아요", description = "댓글 좋아요 등록/취소 API")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @Operation(summary = "댓글 좋아요 토글")
    @PostMapping
    public ResponseEntity<String> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user
    ) {
        boolean liked = commentLikeService.toggleLike(commentId, user);
        return ResponseEntity.ok(liked ? "좋아요 등록됨" : "좋아요 취소됨");
    }

    @Operation(summary = "댓글 좋아요 수 조회")
    @GetMapping
    public ResponseEntity<Integer> getCommentLikeCount(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentLikeService.getLikeCount(commentId));
    }
}