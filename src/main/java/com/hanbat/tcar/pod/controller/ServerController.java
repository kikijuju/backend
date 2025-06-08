package com.hanbat.tcar.pod.controller;

import com.hanbat.tcar.pod.dto.*;
import com.hanbat.tcar.pod.entity.PodListInfoDto;
import com.hanbat.tcar.pod.service.ExternalPodService;
import com.hanbat.tcar.pod.service.PodQueryService;
import com.hanbat.tcar.pod.service.PreSignedUrlService;
import com.hanbat.tcar.user.dto.SimpleMessageResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/container")
@RequiredArgsConstructor
public class ServerController {

    private final PreSignedUrlService preSignedUrlService;
    private final ExternalPodService externalPodService;
    private final PodQueryService podQueryService;


    /* ─────────────────────────────────────────────
     *  1) 새 컨테이너 생성 & Pre-Signed URL 발급
     * ───────────────────────────────────────────── */
    @PostMapping("/create")
    public ResponseEntity<PreSignedUrlResponseDto> createAndPresign(
            @RequestBody OSInfoRequestDto req,
            Authentication auth) {

        return withAuth(auth, email -> {
            log.info("[create] OS={}, ver={}", req.getOs(), req.getVersion());

            PreSignedUrlResponseDto dto = preSignedUrlService.generateForNewContainer(req, email);

            HttpStatus status = dto.getPreSignedUrl().isEmpty()
                    ? HttpStatus.INTERNAL_SERVER_ERROR
                    : HttpStatus.CREATED;
            return new ResponseEntity<>(dto, status);
        });
    }

    /* ─────────────────────────────────────────────
     *  2) 기존 Pod 선택 → Pre-Signed URL 발급
     * ───────────────────────────────────────────── */
    @PostMapping("/presign")
    public ResponseEntity<PreSignedUrlResponseDto> presignExisting(
            @RequestBody PodSelectionRequestDto sel,
            Authentication auth) {

        return withAuth(auth, email -> {
            log.info("[presign] {}", sel);

            PreSignedUrlResponseDto dto = preSignedUrlService.generateForExistingContainer(sel, email);
            HttpStatus status = dto.getPreSignedUrl().isEmpty()
                    ? HttpStatus.BAD_REQUEST
                    : HttpStatus.CREATED;
            return new ResponseEntity<>(dto, status);
        });
    }

    /* ─────────────────────────────────────────────
     *  3) 내 Pod 목록 조회
     * ───────────────────────────────────────────── */
    @GetMapping("/pods")
    public ResponseEntity<List<PodListResponseDto>> listUserPods(Authentication auth) {
        return withAuth(auth, email ->
                ResponseEntity.ok(podQueryService.getPodsForFront(email)));
    }

    /* ─────────────────────────────────────────────
     *  4) Pod 삭제
     *     - 외부 서버엔 podNamespace/podName 만 전송
     *     - 사전 검증: 해당 Pod 가 사용자 소유인지 확인
     * ───────────────────────────────────────────── */
    @PostMapping("/delete")
    public ResponseEntity<SimpleMessageResponseDto> deletePod(
            @RequestBody ContainerDeleteRequest req,
            Authentication auth) {

        return withAuth(auth, email -> {
            log.info("[delete] {}", req);

            boolean mine = externalPodService.fetchUserPods(email).stream()
                    .anyMatch(p -> p.getNamespace().equals(req.getPodNamespace()) &&
                            p.getPodName().equals(req.getPodName()));

            if (!mine) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(msg("Forbidden: Pod does not belong to user"));
            }

            boolean ok = externalPodService.deleteContainer(req);
            return ok
                    ? ResponseEntity.ok(msg("Container deleted successfully"))
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(msg("Container deletion failed"));
        });
    }

    /* ─────────────────────────────────────────────
     *  공통: 인증 체크 & 람다 실행
     * ───────────────────────────────────────────── */
    private <T> ResponseEntity<T> withAuth(Authentication auth,
                                           java.util.function.Function<String, ResponseEntity<T>> fn) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = (String) auth.getPrincipal();
        return fn.apply(email);
    }

    private SimpleMessageResponseDto msg(String m) {
        return new SimpleMessageResponseDto(m);
    }
}