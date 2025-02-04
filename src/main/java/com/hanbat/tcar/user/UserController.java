package com.hanbat.tcar.user;

import com.hanbat.tcar.common.JWToken;
import com.hanbat.tcar.common.JwtGenerator;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("api/users/")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://192.168.1.48:3000")

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
}

