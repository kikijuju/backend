package com.hanbat.tcar.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NaturalId;

@NoArgsConstructor(access = AccessLevel.PROTECTED) //기본 생성자를 protected로 설정하기
@AllArgsConstructor
@Getter
@Entity
@Builder
@Table(name = "user") // 테이블 이름 명확히 지정
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 ID


    @NaturalId
    @Column(nullable = false, unique=true)
    private String email; //유저 이메일 , 아이디로 사용


    @JsonIgnore
    @Column(nullable = false)
    private String password; //비밀번호


    @Column(nullable = false)
    private UserRole role; //등급, 권한

    @Column(nullable = false)
    private String username; //사용자 이름

    @Column(nullable = false, unique = true, length=32)
    private String nickname; // 사용자 닉네임

}
