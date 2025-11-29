package com.hanbat.tcar.config;

import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.entity.Gender;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.entity.UserTier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class UserInitDataConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public org.springframework.boot.CommandLineRunner initUsers() {
        return args -> {
            createIfNotExists(
                    "basic@test.com",
                    "BasicUser",
                    "basicUser",
                    "01000000001",
                    UserTier.BASIC
            );
            createIfNotExists(
                    "pro@test.com",
                    "ProUser",
                    "proUser",
                    "01000000002",
                    UserTier.PRO
            );
            createIfNotExists(
                    "enterprise@test.com",
                    "EnterpriseUser",
                    "enterpriseUser",
                    "01000000003",
                    UserTier.ENTERPRISE
            );
            createIfNotExists(
                    "admin@test.com",
                    "AdminUser",
                    "adminUser",
                    "01000000004",
                    UserTier.ADMIN
            );
        };
    }

    private void createIfNotExists(String email,
                                   String username,
                                   String nickname,
                                   String phone,
                                   UserTier tier) {

        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        String rawPassword = "password123!";
        String encoded = passwordEncoder.encode(rawPassword);

        User user = User.builder()
                .email(email)
                .password(encoded)
                .tier(UserTier.BASIC)
                .tier(tier)                // 실제 서버 사용 제한에 쓰일 등급
                .username(username)
                .nickname(nickname)
                .phoneNumber(phone)
                .birthDate(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)       // Gender enum 값에 맞게 하나 선택
                .build();

        userRepository.save(user);
    }
}
