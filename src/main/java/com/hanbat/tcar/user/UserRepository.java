package com.hanbat.tcar.user;

import com.hanbat.tcar.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    // 이메일 중복 여부
    boolean existsByEmail(String email);
    // 전화번호 중복 여부
    boolean existsByPhoneNumber(String phoneNumber);
    // 닉네임 중복 여부
    boolean existsByNickname(String nickname);
}
