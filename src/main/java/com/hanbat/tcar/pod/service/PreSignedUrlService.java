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
import com.hanbat.tcar.user.service.UserTierPolicyService;
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
    private final PreSignedUrlBuilder urlBuilder;
    private final UserTierPolicyService userTierPolicyService;   // ★ 티어 정책

    /* ─────────────────────────────────────────────
     *  새 컨테이너(Pod) 생성 → Pre-Signed URL 발급
     * ───────────────────────────────────────────── */
    public PreSignedUrlResponseDto generateForNewContainer(OSInfoRequestDto req,
                                                           String tokenEmail) {

        // 1) 유저 조회
        User user = userRepository.findByEmail(tokenEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) 현재 사용자의 Pod 개수 조회
        List<PodListInfoDto> myPods = externalPodService.fetchUserPods(tokenEmail);
        int currentCount = myPods.size();

        // 2-1) 티어별 최대 개수 초과 여부 체크
        if (!userTierPolicyService.canCreateNewPod(user, currentCount)) {
            log.warn("User={} tier={} - pod limit exceeded (current={})",
                    user.getEmail(), user.getTier(), currentCount);
            return fail("Pod limit exceeded for your tier");
        }

        // 2-2) 티어별 허용 OS / 버전인지 체크
        if (!userTierPolicyService.isOsAllowed(user, req.getOs(), req.getVersion())) {
            log.warn("User={} tier={} - OS not allowed: {} {}",
                    user.getEmail(), user.getTier(), req.getOs(), req.getVersion());
            return fail("Selected OS/version is not allowed for your tier");
        }

        // 3) 외부 가상서버에 컨테이너 생성 요청
        ContainerCreateRequest createReq = new ContainerCreateRequest(
                req.getOs(),
                req.getVersion(),
                req.getServerName(),   // calledName 으로 전달됨
                tokenEmail
        );

        PodInfo pod = externalPodService.createContainer(createReq)
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve container info"));

        // 4) 컨테이너 정보로 presigned용 JWT 발급
        String jwt = jwtGenerator.generateTokenWithContainerInfo(
                user, pod.getPodName(), pod.getPodNamespace(), pod.getIngress());

        // 5) presigned URL 생성
        String url = urlBuilder.build(preSignedUrlConfig, jwt, pod);

        log.info("New PresignedURL generated for user={} podName={} namespace={} -> {}",
                user.getEmail(), pod.getPodName(), pod.getPodNamespace(), url);

        return new PreSignedUrlResponseDto(url, "Pre-signed URL generated");
    }

    /* ─────────────────────────────────────────────
     *  기존 Pod 선택 → Pre-Signed URL 발급
     *  (여긴 티어 제한 X, 이미 존재하는 Pod만 대상으로 함)
     * ───────────────────────────────────────────── */
    public PreSignedUrlResponseDto generateForExistingContainer(PodSelectionRequestDto sel,
                                                                String tokenEmail) {

        User user = userRepository.findByEmail(tokenEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<PodListInfoDto> pods = externalPodService.fetchUserPods(tokenEmail);

        boolean valid = pods.stream().anyMatch(p ->
                p.getNamespace().equals(sel.getPodNamespace()) &&
                        p.getPodName().equals(sel.getPodName()) &&
                        p.getIngressUrl().equals(sel.getIngressUrl())
        );
        if (!valid) {
            return fail("Invalid Pod selection");
        }

        String jwt = jwtGenerator.generateTokenWithContainerInfo(
                user, sel.getPodName(), sel.getPodNamespace(), sel.getIngressUrl());

        PodInfo pod = new PodInfo(
                sel.getPodName(),
                sel.getPodNamespace(),
                sel.getIngressUrl()
        );

        String url = urlBuilder.build(preSignedUrlConfig, jwt, pod);

        log.info("Existing PresignedURL generated for user={} podName={} namespace={} -> {}",
                user.getEmail(), pod.getPodName(), pod.getPodNamespace(), url);

        return new PreSignedUrlResponseDto(url, "Pre-signed URL generated for existing Pod");
    }

    private PreSignedUrlResponseDto fail(String msg) {
        return new PreSignedUrlResponseDto("", msg);
    }
}
