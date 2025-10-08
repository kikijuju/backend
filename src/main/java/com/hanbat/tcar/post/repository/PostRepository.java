package com.hanbat.tcar.post.repository;

import com.hanbat.tcar.post.entity.Post;
import com.hanbat.tcar.post.entity.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    // 카테고리로 게시글 조회 (페이징)
    Page<Post> findAllByCategory(PostCategory category, Pageable pageable);
    // 검색을 확장하고 싶다면 → title/author.nickname 포함한 query method 추가도 가능


    // 핀 고정된 게시글을 최신순으로 조회하는 쿼리
    List<Post> findAllByIsPinnedTrueOrderByCreatedAtDesc();
}