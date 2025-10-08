package com.hanbat.tcar.post.entity;

import com.hanbat.tcar.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // 제목
    @Column(nullable = false, length = 200)
    private String title;

    // 본문
    @Lob
    @Column(nullable = false)
    private String content;

    // 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostCategory category;

    // 조회수
    @Column(nullable = false, columnDefinition = "bigint default 0")
    private Long viewCount;

    // 공개 여부
    @Column(nullable = false)
    private boolean isPublic;

    // 생성일
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정일
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 고정핀
    @Column(nullable = false)
    private boolean isPinned;

    // 생성 시점 처리
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.viewCount = 0L;
        this.isPinned = false;
    }

    // 수정 시점 처리
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 게시글 수정 로직
    public void update(String title, String content, PostCategory category, boolean isPublic) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isPublic = isPublic;
    }

    // 조회수 증가 로직
    public void increaseViewCount(long count) {
        this.viewCount += count;
    }

    public void setPinned(boolean pinned) {
        this.isPinned = pinned;
    }
    // 핀 토글 메서드
    public void togglePinned() {
        this.isPinned = !this.isPinned;
    }
}