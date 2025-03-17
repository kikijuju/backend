package com.hanbat.tcar.user;

import com.hanbat.tcar.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByNickname(String nickname);
    // 이메일 중복 여부
    boolean existsByEmail(String email);
}
