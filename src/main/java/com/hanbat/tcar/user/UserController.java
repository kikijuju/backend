package com.hanbat.tcar.user;

import com.hanbat.tcar.common.JWToken;
import com.hanbat.tcar.common.JwtGenerator;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/users/")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://192.168.1.48:3000")

public class UserController {
    private final UserService userService;
    private final JwtGenerator jwtGenerator;

    @Tag(name="signup", description="회원가입")
    @PostMapping("/signup")
//    @ApiResponse(responseCode = "200", description = "회원가입 성공")
//            content = @Content(schema = @Schema(implementation = User.class)))
//    @ApiResponse(responseCode = "400", description = "회원가입 실패")
    public ResponseEntity<UserSignupResponseDto> signUp(@RequestBody UserSignupRequestDto userSignupRequestDto){
        try{
            User user = userService.signUp(userSignupRequestDto);
            UserSignupResponseDto userSignupResponseDto = UserSignupResponseDto.builder()
                    .message("회원가입이 완료되었습니다")
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(userSignupResponseDto);

        } catch(ResponseStatusException e){
            UserSignupResponseDto userSignupResponseDto = UserSignupResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(userSignupResponseDto);
        }
    }

    @Tag(name="login", description="로그인")
    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto userLoginRequestDto){
        try{
            User user = userService.userFind(userLoginRequestDto);

            JWToken token = jwtGenerator.generateToken(user);

            UserLoginResponseDto userLoginResponseDto = UserLoginResponseDto.builder()
                    .accessToken(token.getAccessToken())
                    .refreshToken(token.getRefreshToken())
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(userLoginResponseDto);
        } catch (ResponseStatusException e){
            UserLoginFailureResponseDto userLoginFailureResponseDto = UserLoginFailureResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(userLoginFailureResponseDto);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMyEmail(Authentication authentication) {
        if (authentication == null) {
            // 토큰이 없거나 유효하지 않아 인증에 실패한 경우
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증 실패: 토큰이 없습니다 or 잘못되었습니다.");
        }
        // JwtFilter 에서 principal 에 email 을 넣었다면 여기서 꺼낼 수 있음
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok("인증된 사용자 이메일: " + email);
    }

}

