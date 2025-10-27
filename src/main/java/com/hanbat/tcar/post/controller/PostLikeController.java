package com.hanbat.tcar.post.controller;

import com.hanbat.tcar.post.service.PostLikeService;
import com.hanbat.tcar.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "게시글 좋아요", description = "게시글 좋아요 등록/취소 기능")
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 좋아요 토글", description = "현재 유저가 게시글에 좋아요를 눌렀는지 여부에 따라 등록 또는 취소합니다.")
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> toggleLike(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        boolean liked = postLikeService.toggleLike(postId, user);
        return ResponseEntity.ok(liked ? "좋아요가 등록되었습니다." : "좋아요가 취소되었습니다.");
    }

    @Operation(summary = "좋아요 수 조회", description = "게시글에 달린 총 좋아요 수를 반환합니다.")
    @GetMapping("/{postId}/like")
    public ResponseEntity<Integer> getLikeCount(@PathVariable Long postId) {
        return ResponseEntity.ok(postLikeService.getLikeCount(postId));
    }

    @Operation(summary = "좋아요 여부 확인", description = "현재 로그인한 사용자가 게시글에 좋아요를 눌렀는지 여부를 반환합니다.")
    @GetMapping("/{postId}/like/status")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        boolean isLiked = postLikeService.isLiked(postId, user);
        return ResponseEntity.ok(isLiked);
    }
}