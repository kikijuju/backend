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
    private final PreSignedUrlBuilder urlBuilder;

    /* ─────────────────────────────────────────────
     *  새 컨테이너(Pod) 생성 → Pre-Signed URL 발급
     * ───────────────────────────────────────────── */
    public PreSignedUrlResponseDto generateForNewContainer(OSInfoRequestDto req,
                                                           String tokenEmail) {

        User user = userRepository.findByEmail(tokenEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ContainerCreateRequest createReq = new ContainerCreateRequest(
                req.getOs(),
                req.getVersion(),
                req.getServerName(),
                tokenEmail
        );

        PodInfo pod = externalPodService.createContainer(createReq)
                .orElseThrow(() -> new IllegalStateException("Failed to retrieve container info"));

        String jwt = jwtGenerator.generateTokenWithContainerInfo(
                user, pod.getPodName(), pod.getPodNamespace(), pod.getIngress());

        String url = urlBuilder.build(preSignedUrlConfig, jwt, pod);

        log.info("New PresignedURL generated for user={} podName={} namespace={} -> {}",
                user.getEmail(), pod.getPodName(), pod.getPodNamespace(), url);

        return new PreSignedUrlResponseDto(url, "Pre-signed URL generated");
    }

    /* ─────────────────────────────────────────────
     *  기존 Pod 선택 → Pre-Signed URL 발급
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

        PodInfo pod = new PodInfo(sel.getPodName(),
                sel.getPodNamespace(),
                sel.getIngressUrl());

        String url = urlBuilder.build(preSignedUrlConfig, jwt, pod);

        log.info("Existing PresignedURL generated for user={} podName={} namespace={} -> {}",
                user.getEmail(), pod.getPodName(), pod.getPodNamespace(), url);

        return new PreSignedUrlResponseDto(url, "Pre-signed URL generated for existing Pod");
    }

    private PreSignedUrlResponseDto fail(String msg) {
        return new PreSignedUrlResponseDto("", msg);
    }
}
