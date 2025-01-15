package com.hanbat.tcar.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;


    //회원가입 시
    public String signUp(UserSignupDto userSignupDto){
        Optional<User> findUser = userRepository.findByEmail(userSignupDto.getEmail());
        if(findUser.isPresent()){
            return "이미 존재하는 이메일 입니다.";
        }

        User user = User.builder()
                .email(userSignupDto.getEmail())
                .password(userSignupDto.getPassword()) //TODO 비밀번호 해시화
                .username(userSignupDto.getUsername())
                .nickname(userSignupDto.getNickname())
                .role(UserRole.BASIC)
                .build();

        userRepository.save(user);
        return "회원가입 성공";
    }


}
