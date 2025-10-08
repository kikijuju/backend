package com.hanbat.tcar.post.controller;

import com.hanbat.tcar.post.dto.*;
import com.hanbat.tcar.post.entity.PostCategory;
import com.hanbat.tcar.post.service.PostService;
import com.hanbat.tcar.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "게시글", description = "게시글 CRUD 및 목록 조회 API")
public class PostController {

    private final PostService postService;

    @Operation(summary = "전체 게시글 목록 조회", description = "페이지네이션 및 정렬이 적용된 전체 게시글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPosts(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(postService.getPostList(pageable));
    }

    @Operation(summary = "카테고리별 게시글 목록 조회", description = "지정한 카테고리의 게시글만 페이지네이션하여 조회합니다.")
    @GetMapping(params = "category")
    public ResponseEntity<Page<PostResponse>> getPostsByCategory(
            @Parameter(description = "카테고리 이름 (ex: NOTICE, FREE, QUESTION, TIP)", required = true)
            @RequestParam PostCategory category,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPostsByCategory(category, pageable));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID를 통해 단일 게시글 정보를 조회하고, 조회수를 1 증가시킵니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(postService.getPost(postId, user, request));
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다. 로그인된 사용자만 작성할 수 있습니다.")
    @PostMapping
    public ResponseEntity<Long> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        Long postId = postService.createPost(request, user);
        return ResponseEntity.ok(postId);
    }

    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다. 작성자 본인만 수정할 수 있습니다.")
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        postService.updatePost(postId, request, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. 작성자 또는 관리자가 삭제할 수 있습니다.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "게시글 ID", required = true)
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        postService.deletePost(postId, user);
        return ResponseEntity.noContent().build();
    }
}