// 게시글 관련 비즈니스 로직을 담당하는 서비스 클래스
package com.hanbat.tcar.post.service;

import com.hanbat.tcar.comment.repository.CommentRepository;
import com.hanbat.tcar.post.dto.PostCreateRequest;
import com.hanbat.tcar.post.dto.PostResponse;
import com.hanbat.tcar.post.dto.PostUpdateRequest;
import com.hanbat.tcar.post.entity.Post;
import com.hanbat.tcar.post.entity.PostCategory;
import com.hanbat.tcar.post.repository.PostLikeRepository;
import com.hanbat.tcar.post.repository.PostRepository;
import com.hanbat.tcar.user.entity.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostLikeService postLikeService;
    private final StringRedisTemplate redisTemplate;

    private static final Duration VIEW_TTL = Duration.ofHours(1);

    // 전체 게시글 목록 조회 (페이징 + viewCount/commentCount/likeCount 포함, isLiked는 false)
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostList(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(post -> {
                    int viewCount = getViewCount(post.getId());
                    int commentCount = commentRepository.countByPost(post);
                    int likeCount = postLikeRepository.countByPost(post);
                    return PostResponse.from(post, viewCount, commentCount, likeCount); // 목록은 isLiked 제외
                });
    }

    // 카테고리별 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByCategory(PostCategory category, Pageable pageable) {
        return postRepository.findAllByCategory(category, pageable)
                .map(post -> {
                    int viewCount = getViewCount(post.getId());
                    int commentCount = commentRepository.countByPost(post);
                    int likeCount = postLikeRepository.countByPost(post);
                    return PostResponse.from(post, viewCount, commentCount, likeCount); // 목록은 isLiked 제외
                });
    }

    // 게시글 상세 조회 (조회수 증가 및 isLiked 포함 응답)
    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId, User user, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시글이 존재하지 않습니다."));

        String viewerKey = generateViewerKey(postId, user, request);
        String viewCountKey = "post:viewCount:" + postId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(viewerKey))) {
            redisTemplate.opsForValue().increment(viewCountKey);
            redisTemplate.opsForValue().set(viewerKey, "1", VIEW_TTL);
        }

        int viewCount = getViewCount(postId);
        int commentCount = commentRepository.countByPost(post);
        int likeCount = postLikeRepository.countByPost(post);
        boolean isLiked = postLikeService.isLiked(postId, user);

        return PostResponse.from(post, viewCount, commentCount, likeCount, isLiked);
    }

    // 게시글 생성
    @Transactional
    public Long createPost(PostCreateRequest request, User author) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .category(request.getCategory())
                .isPublic(true)
                .author(author)
                .build();

        return postRepository.save(post).getId();
    }

    // 조회수만 증가시키는 내부 로직 (비공식용)
    @Transactional(readOnly = true)
    public Post viewPost(Long postId, User user, HttpServletRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        String viewerKey = generateViewerKey(postId, user, request);
        String viewCountKey = "post:viewCount:" + postId;

        if (Boolean.FALSE.equals(redisTemplate.hasKey(viewerKey))) {
            redisTemplate.opsForValue().increment(viewCountKey);
            redisTemplate.opsForValue().set(viewerKey, "1", VIEW_TTL);
        }

        return post;
    }

    // 게시글 수정
    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(currentUser.getId())) {
            throw new SecurityException("게시글 작성자만 수정할 수 있습니다.");
        }

        post.update(request.getTitle(), request.getContent(), request.getCategory(), request.isPublic());
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

        if (!post.getAuthor().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new SecurityException("작성자 또는 관리자만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    // 중복 조회 방지용 Redis 키 생성 (userId 또는 IP 기반)
    private String generateViewerKey(Long postId, User user, HttpServletRequest request) {
        if (user != null) {
            return "post:viewed:" + postId + ":user:" + user.getId();
        } else {
            String ip = getClientIP(request);
            return "post:viewed:" + postId + ":ip:" + ip;
        }
    }

    // 클라이언트 IP 주소 추출
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // Redis에 저장된 viewCount 반환 (없으면 0)
    public int getViewCount(Long postId) {
        String viewCountKey = "post:viewCount:" + postId;
        String cached = redisTemplate.opsForValue().get(viewCountKey);
        return cached != null ? Integer.parseInt(cached) : 0;
    }
    // 핀 설정/해제 메서드
    @Transactional
    public void setPinStatus(Long postId, User user, boolean pinned) {
        if (!user.getRole().name().equals("ADMIN")) {
            throw new SecurityException("관리자만 핀 설정/해제가 가능합니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글이 존재하지 않습니다."));

        post.setPinned(pinned);
    }

    // 핀 고정된 게시글 목록 조회
    @Transactional(readOnly = true)
    public List<PostResponse> getPinnedPosts() {
        return postRepository.findAllByIsPinnedTrueOrderByCreatedAtDesc().stream()
                .map(post -> {
                    int viewCount = getViewCount(post.getId());
                    int commentCount = commentRepository.countByPost(post);
                    int likeCount = postLikeRepository.countByPost(post);
                    return PostResponse.from(post, viewCount, commentCount, likeCount);
                }).toList();
    }
}