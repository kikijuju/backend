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


    //회원가입 시
    public User signUp(UserSignupRequestDto userSignupRequestDto){
        Optional<User> findUser = userRepository.findByEmail(userSignupRequestDto.getEmail());
        if(userRepository.existsByEmail(userSignupRequestDto.getEmail())){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다.");
        }

        // 닉네임 중복 확인
        if(userRepository.existsByNickname(userSignupRequestDto.getNickname())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임 입니다");
        }

        // 비밀번호와 확인 비밀번호 일치 여부 확인
        if (!userSignupRequestDto.getPassword().equals(userSignupRequestDto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다.");
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

    private void validatePassword(String password, UserSignupRequestDto dto) {
        // 1. 최소 길이: 최소 10자 이상
        if (password.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 최소 10자 이상이어야 합니다.");
        }

        // 2. 복잡성 요구: 영문,숫자, 특수문자를 모두 포함해야 함
        // (?=.*\\d)   : 최소 하나의 숫자 포함만야
        // (?=.*[@$!%*?&]) : 최소 하나의 특수문자 포함
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호는 대문자, 소문자, 숫자, 특수문자를 모두 포함해야 합니다.");
        }

        // 3. 개인정보 제외: 이메일 정보를 포함하면 안 됨
        if (dto.getEmail() != null) {
            int atIndex = dto.getEmail().indexOf('@');
            if (atIndex != -1) {
                String localEmailPart = dto.getEmail().substring(0, atIndex);
                if (password.toLowerCase().contains(localEmailPart.toLowerCase())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호에 이메일 정보를 포함할 수 없습니다.");
                }
            }
        }

        // 4. 연속 문자/반복 문자 제한: 동일 문자가 3번 이상 연속하거나, 3자리 이상의 연속 증가/감소하는 숫자/문자 사용 제한
        // 동일 문자 반복 제한
        if (password.matches(".*(.)\\1\\1.*")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호에 동일한 문자를 3번 이상 연속 사용할 수 없습니다.");
        }

        // 연속된 문자/숫자 제한
        if (hasSequentialCharacters(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호에 연속되는 문자나 숫자는 사용할 수 없습니다.");
        }
    }

    private boolean hasSequentialCharacters(String password) {
        // 문자열 길이가 3 미만이면 검사할 필요 없음
        if (password.length() < 3) {
            return false;
        }
        for (int i = 0; i < password.length() - 2; i++) {
            int first = password.charAt(i);
            int second = password.charAt(i + 1);
            int third = password.charAt(i + 2);
            // 오름차순 연속 검사 (예: abc, 123)
            if (second == first + 1 && third == second + 1) {
                return true;
            }
            // 내림차순 연속 검사 (예: cba, 321)
            if (second == first - 1 && third == second - 1) {
                return true;
            }
        }
        return false;
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



}
