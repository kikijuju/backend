package com.hanbat.tcar.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Builder
@Table(name = "user")   // 필요하면 나중에 users 로 바꿔도 됨
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 ID

    @NaturalId
    @Column(nullable = false, unique = true)
    private String email; // 유저 이메일 , 아이디로 사용

    @JsonIgnore
    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String username; // 사용자 이름

    @Column(nullable = false, unique = true, length = 32)
    private String nickname; // 사용자 닉네임

    @Column(nullable = false)
    private String phoneNumber; // 휴대폰 번호

    @Column(nullable = false)
    private LocalDate birthDate; // 생년월일

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender; // 성별

    // ★ 등급/권한 – UserTier 하나만 사용
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTier tier = UserTier.BASIC;

    // ====== 비즈니스 메서드 ======

    // 회원 정보 수정
    public void updateAccountInfo(String nickname,
                                  String username,
                                  LocalDate birthDate,
                                  Gender gender) {
        this.nickname = nickname;
        this.username = username;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    // 등급 변경 (ADMIN 페이지에서 사용)
    public void setTier(UserTier tier) {
        this.tier = tier;
    }

    // 비밀번호 변경
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
