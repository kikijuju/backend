package com.hanbat.tcar.user.service;

import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.dto.UserLoginRequestDto;
import com.hanbat.tcar.user.dto.UserSignupRequestDto;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.entity.UserRole;
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


    //회원가입 때 DB로 넘어갈 정보
    @Transactional
    public User signUp(UserSignupRequestDto userSignupRequestDto){

        // 이메일 중복 확인
        if (userRepository.existsByEmail(userSignupRequestDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        }

        // 전화번호 중복 확인
        if (userRepository.existsByPhoneNumber(userSignupRequestDto.getPhoneNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 전화번호입니다.");
        }

        // 비밀번호 형식 검증
        if (!UserService.isValidPasswordFormat(userSignupRequestDto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다.");
        }


        User user = User.builder()
                .email(userSignupRequestDto.getEmail())
                .password(passwordEncoder.encode(userSignupRequestDto.getPassword()))
                .username(userSignupRequestDto.getUsername())
                .nickname(userSignupRequestDto.getUsername())
                .phoneNumber(userSignupRequestDto.getPhoneNumber())
                .birthDate(userSignupRequestDto.getBirthDate())  // LocalDate 타입이어야 함
                .gender(userSignupRequestDto.getGender())        // Gender enum 값
                .role(UserRole.BASIC)
                .build();

        userRepository.save(user);

        return user;

    }


    public User userFind(UserLoginRequestDto userLoginRequestDto){
        //이메일 먼저 존재하는지 확인
        Optional<User> findUser  = userRepository.findByEmail(userLoginRequestDto.getEmail());
        if(findUser.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다");
        }

        User user = findUser.get();
        // 비밀번호 일치하는 지 확인
        if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디 또는 비밀번호가 일치하지 않습니다");
        }

        return user;

    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 이메일 검증
    public void checkDuplicatedEmail(String email) {
        if(userRepository.existsByEmail(email)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다.");
        }
    }

    // 전화번호 중복
    public void checkDuplicatedPhoneNumber(String phoneNumber) {
        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용중인 전화번호입니다.");
        }
    }

    //이메일 중복확인
    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
    // 비밀번호
    public static boolean isValidPasswordFormat(String password) {
        // 8자 이상, 영문/숫자/특수문자 혼합
        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=]).{8,}$";
        return password.matches(regex);
    }

}
