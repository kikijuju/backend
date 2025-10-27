package com.hanbat.tcar.comment.controller;

import com.hanbat.tcar.comment.dto.*;
import com.hanbat.tcar.comment.service.CommentService;
import com.hanbat.tcar.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
@Tag(name = "댓글", description = "게시글 댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성")
    @PostMapping
    public ResponseEntity<Long> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(commentService.writeComment(postId, request, user));
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(commentService.getComments(postId, user));
    }

    @Operation(summary = "댓글 수정")
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        commentService.updateComment(commentId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user
    ) {
        commentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }
}