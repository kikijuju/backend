package com.hanbat.tcar.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

@Getter
@Setter
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 기본 ID


    @NaturalId
    @Column(nullable = false, unique=true)
    private String email; //유저 이메일 , 아이디로 사용


    @Column(nullable = false)
    private String password; //비밀번호


    @Column(nullable = false)
    private UserRole role; //등급, 권한

    @Column(nullable = false)
    private String username; //사용자 이름

    @Column(nullable = false, unique = true, length=32)
    private String nickname; // 사용자 닉네임

}
