package com.hanbat.tcar.account.service;

import com.hanbat.tcar.account.dto.AccountUpdateRequestDto;
import com.hanbat.tcar.account.dto.PasswordChangeRequestDto;
import com.hanbat.tcar.auth.service.RefreshTokenService;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.service.UserService;
import com.hanbat.tcar.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    // 회원탈퇴 기능
    @Transactional
    public void deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));
        refreshTokenService.deleteRefreshTokenForUser(user.getId());
        userRepository.delete(user);
    }
    //회원수정 기능
    @Transactional
    public void updateUserInfo(String email, AccountUpdateRequestDto request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // 닉네임이 변경됐고, 중복이라면 예외 처리
        if (!user.getNickname().equals(request.getNickname()) &&
                userRepository.existsByNickname(request.getNickname())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        // 정보 업데이트
        user.updateAccountInfo(request.getNickname(), request.getUsername(), request.getBirthDate(), request.getGender());
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String email, PasswordChangeRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }
        // 새 비밀번호 형식 검증
        if (!UserService.isValidPasswordFormat(requestDto.getNewPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "비밀번호 형식이 올바르지 않습니다.");
        }

        if (!requestDto.getNewPassword().equals(requestDto.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호가 일치하지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(requestDto.getNewPassword()));
    }
}