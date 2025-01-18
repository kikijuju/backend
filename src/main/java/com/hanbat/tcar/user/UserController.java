package com.hanbat.tcar.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("api/users/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @PostMapping("/signup")
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
}

