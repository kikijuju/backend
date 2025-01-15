package com.hanbat.tcar.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserSignupDto userSignupDto){
        String result = userService.signUp(userSignupDto);

        //TODO 만약에 회원가입 실패했다면? 예외처리 작성하기
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
