package com.hanbat.tcar.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    //회원가입 시
    public User signUp(UserSignupRequestDto userSignupRequestDto){
        Optional<User> findUser = userRepository.findByEmail(userSignupRequestDto.getEmail());
        if(findUser.isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일 입니다");
        }


        User user = User.builder()
                .email(userSignupRequestDto.getEmail())
                .password(passwordEncoder.encode(userSignupRequestDto.getPassword()))
                .username(userSignupRequestDto.getUsername())
                .nickname(userSignupRequestDto.getNickname())
                .role(UserRole.BASIC)
                .build();

        userRepository.save(user);

        return user;

    }


}
