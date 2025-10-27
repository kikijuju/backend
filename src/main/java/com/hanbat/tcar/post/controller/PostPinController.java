// 게시글 핀 고정 및 해제 컨트롤러
package com.hanbat.tcar.post.controller;

import com.hanbat.tcar.post.service.PostService;
import com.hanbat.tcar.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "게시글 핀 고정", description = "관리자가 게시글을 상단 고정 또는 해제합니다.")
public class PostPinController {

    private final PostService postService;

    @Operation(summary = "게시글 상단 고정", description = "관리자만 게시글을 상단 고정할 수 있습니다.")
    @PostMapping("/{postId}/pin")
    public ResponseEntity<Void> pinPost(@PathVariable Long postId, @AuthenticationPrincipal User user) {
        postService.setPinStatus(postId, user, true);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "게시글 상단 고정 해제", description = "관리자만 게시글 상단 고정을 해제할 수 있습니다.")
    @DeleteMapping("/{postId}/pin")
    public ResponseEntity<Void> unpinPost(@PathVariable Long postId, @AuthenticationPrincipal User user) {
        postService.setPinStatus(postId, user, false);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상단 고정 게시글 목록 조회", description = "고정된 게시글만 최신순으로 조회합니다.")
    @GetMapping("/pinned")
    public ResponseEntity<?> getPinnedPosts() {
        return ResponseEntity.ok(postService.getPinnedPosts());
    }
}