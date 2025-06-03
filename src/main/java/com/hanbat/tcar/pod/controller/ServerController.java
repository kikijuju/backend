package com.hanbat.tcar.pod.controller;

import com.hanbat.tcar.config.PreSignedUrlConfig;
import com.hanbat.tcar.auth.jwt.JwtGenerator;
import com.hanbat.tcar.pod.dto.ServerDeletionRequestDto;
import com.hanbat.tcar.pod.dto.OSInfoRequestDto;
import com.hanbat.tcar.pod.dto.PreSignedUrlResponseDto;
import com.hanbat.tcar.pod.entity.PodInfoResponseDto;
import com.hanbat.tcar.pod.entity.PodListInfoDto;
import com.hanbat.tcar.pod.service.ExternalPodService;
import com.hanbat.tcar.user.dto.*;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import java.util.List;



@Slf4j
@RestController
@RequestMapping("/api/container/")
@RequiredArgsConstructor
public class ServerController {

    private final ExternalPodService externalContainerService;
    private final UserRepository userRepository;
    private final JwtGenerator jwtGenerator;
    private final PreSignedUrlConfig preSignedUrlConfig;

    @PostMapping("/create")
    public ResponseEntity<PreSignedUrlResponseDto> generatePresignedUrl(
            @RequestBody OSInfoRequestDto osRequest,
            Authentication authentication
    ) {
        log.info("PreSigned URL 요청 받음: OS={}, Version={}", osRequest.getOs(), osRequest.getVersion());

        if (authentication == null) {
            log.warn("⛔ 인증 실패: Authentication is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(PreSignedUrlResponseDto.builder()
                            .preSignedUrl("")
                            .message("Unauthorized: No valid token")
                            .build());
        }

        String tokenEmail = (String) authentication.getPrincipal();
        log.info("🔑 인증된 사용자 이메일: {}", tokenEmail);

        Optional<User> userOpt = userRepository.findByEmail(tokenEmail);
        if (userOpt.isEmpty()) {
            log.warn("❌ 사용자 정보 없음: {}", tokenEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PreSignedUrlResponseDto.builder()
                            .preSignedUrl("")
                            .message("User not found")
                            .build());
        }

        User user = userOpt.get();

        PodInfoResponseDto containerInfo = externalContainerService.getContainerInfo(
                osRequest.getOs(),
                osRequest.getVersion(),
                tokenEmail
        );

        if (containerInfo == null) {
            log.error("🚨 컨테이너 정보 조회 실패: OS={}, Version={}, Email={}",
                    osRequest.getOs(), osRequest.getVersion(), tokenEmail);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PreSignedUrlResponseDto.builder()
                            .preSignedUrl("")
                            .message("Failed to retrieve container info")
                            .build());
        }

        log.info("📦 컨테이너 정보 수신: PodName={}, Namespace={}, Ingress={}",
                containerInfo.getPodName(), containerInfo.getPodNamespace(), containerInfo.getIngress());

        String newToken = jwtGenerator.generateTokenWithContainerInfo(user, containerInfo.getPodName(),
                containerInfo.getPodNamespace(), containerInfo.getIngress());

        String preSignedUrl = "http://192.168.1.23:8080/api/access/presigned/validate?token=" + newToken;
        log.info("🔗 PreSigned URL 생성 완료: {}", preSignedUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                PreSignedUrlResponseDto.builder()
                        .preSignedUrl(preSignedUrl)
                        .message("Pre-signed URL generated successfully")
                        .build()
        );
    }

    @PostMapping("/delete")
    public ResponseEntity<SimpleMessageResponseDto> deleteContainer(
            @RequestBody ServerDeletionRequestDto deletionRequest,
            Authentication authentication) {

        log.info("🗑️ 컨테이너 삭제 요청: PodNamespace={}, PodName={}",
                deletionRequest.getPodNamespace(), deletionRequest.getPodName());

        if (authentication == null) {
            log.warn("⛔ 인증 실패: Authentication is null");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(SimpleMessageResponseDto.builder()
                            .message("Unauthorized: No valid token")
                            .build());
        }

        String userEmail = (String) authentication.getPrincipal();
        log.info("🔑 인증된 사용자 이메일: {}", userEmail);

        boolean deletionSuccess = externalContainerService.deleteContainer(
                userEmail,
                deletionRequest.getPodNamespace(),
                deletionRequest.getPodName()
        );

        if (!deletionSuccess) {
            log.error("❌ 컨테이너 삭제 실패: Email={}, PodNamespace={}, PodName={}",
                    userEmail, deletionRequest.getPodNamespace(), deletionRequest.getPodName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimpleMessageResponseDto.builder()
                            .message("Container deletion failed")
                            .build());
        }

        log.info("✅ 컨테이너 삭제 성공: Email={}, PodNamespace={}, PodName={}",
                userEmail, deletionRequest.getPodNamespace(), deletionRequest.getPodName());

        return ResponseEntity.ok(SimpleMessageResponseDto.builder()
                .message("Container deleted successfully")
                .build());
    }

    @GetMapping("/pods")
    public ResponseEntity<List<PodListInfoDto>> getUserPods(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = (String) authentication.getPrincipal();

        List<PodListInfoDto> podList = externalContainerService.getUserPodList(email);

        return ResponseEntity.ok(podList);
    }
}
