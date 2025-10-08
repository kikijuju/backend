package com.hanbat.tcar.pod.service;

import com.hanbat.tcar.auth.jwt.JwtGenerator;
import com.hanbat.tcar.config.PreSignedUrlConfig;
import com.hanbat.tcar.pod.dto.ContainerCreateRequest;
import com.hanbat.tcar.pod.dto.OSInfoRequestDto;
import com.hanbat.tcar.pod.dto.PodSelectionRequestDto;
import com.hanbat.tcar.pod.dto.PreSignedUrlResponseDto;
import com.hanbat.tcar.pod.entity.PodInfo;
import com.hanbat.tcar.pod.entity.PodListInfoDto;
import com.hanbat.tcar.user.UserRepository;
import com.hanbat.tcar.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreSignedUrlService {

    private final UserRepository userRepository;
    private final ExternalPodService externalPodService;
    private final JwtGenerator jwtGenerator;
    private final PreSignedUrlConfig preSignedUrlConfig;
    private final PreSignedUrlBuilder urlBuilder;   // ★ URL 전담

    /* ─────────────────────────────────────────────
     *  새 컨테이너(Pod) 생성 → Pre-Signed URL 발급
     * ───────────────────────────────────────────── */
    public PreSignedUrlResponseDto generateForNewContainer(OSInfoRequestDto req,
                                                           String tokenEmail) {

        // 1) 사용자 조회
        User user = userRepository.findByEmail(tokenEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) 프론트 DTO → 외부 서버 호출 DTO
        ContainerCreateRequest createReq = new ContainerCreateRequest(
                req.getOs(),            // OS
                req.getVersion(),       // 버전
                req.getServerName(),    // calledName (= serverName)
                tokenEmail              // userEmail
        );

        // 3) Pod 생성
        PodInfo pod = externalPodService.createContainer(createReq)
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve container info"));

        // 4) JWT 생성
        String jwt = jwtGenerator.generateTokenWithContainerInfo(
                user, pod.getPodName(), pod.getPodNamespace(), pod.getIngress());

        // 5) Pre-Signed URL 조립
        String url = urlBuilder.build(preSignedUrlConfig, jwt, pod);

        return new PreSignedUrlResponseDto(url, "Pre-signed URL generated");
    }

    /* ─────────────────────────────────────────────
     *  2) 기존 Pod 선택 → Pre-Signed URL 발급
     * ───────────────────────────────────────────── */
    public PreSignedUrlResponseDto generateForExistingContainer(PodSelectionRequestDto sel,
                                                                String tokenEmail) {

        // ① 사용자 확인
        User user = userRepository.findByEmail(tokenEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ② 내 Pod 목록 조회
        List<PodListInfoDto> pods = externalPodService.fetchUserPods(tokenEmail);

        boolean valid = pods.stream().anyMatch(p ->
                p.getNamespace().equals(sel.getPodNamespace()) &&
                        p.getPodName().equals(sel.getPodName()) &&
                        p.getIngressUrl().equals(sel.getIngressUrl())
        );
        if (!valid) {
            return fail("Invalid Pod selection");
        }

        // ③ JWT
        String jwt = jwtGenerator.generateTokenWithContainerInfo(
                user,
                sel.getPodName(),
                sel.getPodNamespace(),
                sel.getIngressUrl()
        );
        // ④ Pre-Signed URL
        PodInfo pod = new PodInfo(sel.getPodName(),
                sel.getPodNamespace(),
                sel.getIngressUrl());

        String url = urlBuilder.build(preSignedUrlConfig, jwt, pod);

        return new PreSignedUrlResponseDto(url, "Pre-signed URL generated for existing Pod");
    }

    /* ─────────────────────────────────────────────
     *  3) 실패 응답 헬퍼
     * ───────────────────────────────────────────── */
    private PreSignedUrlResponseDto fail(String msg) {
        return new PreSignedUrlResponseDto("", msg);
    }
}