package com.hanbat.tcar.user.controller;

import com.hanbat.tcar.config.PreSignedUrlConfig;
import com.hanbat.tcar.security.jwt.JwtGenerator;
import com.hanbat.tcar.user.dto.*;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.service.ExternalContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/container")
@RequiredArgsConstructor
public class ContainerController {

    private final ExternalContainerService externalContainerService;
    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final PreSignedUrlConfig preSignedUrlConfig;

    @PostMapping("/create")
    public ResponseEntity<PreSignedUrlResponseDto> generatePresignedUrl(
            @RequestBody OSInfoRequestDto osRequest,
            Authentication authentication
    ) {
        // 1) JWT 검증은 JwtFilter에서 이미 처리됨.
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PreSignedUrlResponseDto.builder()
                            .preSignedUrl("")
                            .message("Unauthorized: No valid token")
                            .build());
        }

        // 2) JWT에서 추출한 사용자 이메일을 사용 (여기서 principal은 email로 설정되어 있음)
        String tokenEmail = (String) authentication.getPrincipal();

        // 3) DB에서 사용자 조회 (토큰의 이메일로 조회)
        Optional<User> userOpt = userRepository.findByEmail(tokenEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PreSignedUrlResponseDto.builder()
                            .preSignedUrl("")
                            .message("User not found")
                            .build());
        }
        User user = userOpt.get();

        // 4) 외부 서버에 OS, version, userEmail 정보를 전송하여 container 정보를 획득
        ExternalContainerResponse containerInfo = externalContainerService.getContainerInfo(
                osRequest.getOs(),
                osRequest.getVersion(),
                tokenEmail
        );
        if (containerInfo == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PreSignedUrlResponseDto.builder()
                            .preSignedUrl("")
                            .message("Failed to retrieve container info")
                            .build());
        }

        // 5) port 값을 정수로 변환 ("N/A" 등일 경우 기본값 0 처리)
        String port = containerInfo.getPort();


        // 6) 새로운 JWT(컨테이너 전용 토큰) 생성: 이 토큰에 {userEmail, containerId, port}를 클레임으로 포함
        String newToken = jwtGenerator.generateTokenWithContainerInfo(user, containerInfo.getContainerId(), port);

        // 7) pre-signed URL 생성: 설정된 pre-signed URL + 쿼리 파라미터로 token 추가
        String preSignedUrl = "http://192.168.1.5:8080/api/access/container"+"?token=" + newToken;

        // 8) 결과 DTO 구성 및 반환
        PreSignedUrlResponseDto responseDto = PreSignedUrlResponseDto.builder()
                .preSignedUrl(preSignedUrl)
                .message("Pre-signed URL generated successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    /**
     * 컨테이너 삭제 API
     * 프론트로부터 { "containerId": "..." } 형태로 요청 받고,
     * JWT 검증을 통해 사용자가 인증되어 있는지 확인 후,
     * 사용자 이메일과 컨테이너 ID를 외부 서버에 전송하여 삭제 요청을 수행합니다.
     */
    @PostMapping("/delete")
    public ResponseEntity<SimpleMessageResponseDto> deleteContainer(
            @RequestBody ContainerDeletionRequestDto deletionRequest,
            Authentication authentication) {

        // JWT 검증은 JwtFilter에서 이미 수행되어 Authentication에 사용자 이메일이 설정됨
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SimpleMessageResponseDto.builder()
                            .message("Unauthorized: No valid token")
                            .build());
        }

        String userEmail = (String) authentication.getPrincipal();

        // 외부 서버에 사용자 이메일과 컨테이너 ID 전송하여 삭제 요청 수행
        boolean deletionSuccess = externalContainerService.deleteContainer(userEmail, deletionRequest.getContainerId());

        if (!deletionSuccess) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimpleMessageResponseDto.builder()
                            .message("Container deletion failed")
                            .build());
        }

        return ResponseEntity.ok(SimpleMessageResponseDto.builder()
                .message("Container deleted successfully")
                .build());
    }
}