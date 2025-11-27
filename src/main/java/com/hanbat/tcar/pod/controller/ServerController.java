package com.hanbat.tcar.pod.controller;

import com.hanbat.tcar.pod.dto.*;
import com.hanbat.tcar.pod.entity.PodListInfoDto;
import com.hanbat.tcar.pod.service.ExternalPodService;
import com.hanbat.tcar.pod.service.OsOptionService;
import com.hanbat.tcar.pod.service.PodQueryService;
import com.hanbat.tcar.pod.service.PreSignedUrlService;
import com.hanbat.tcar.user.dto.SimpleMessageResponseDto;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

@Tag(name = "Container", description = "컨테이너(가상 서버) 생성 · 조회 · 삭제 및 OS 옵션 API")
public class ServerController {

    private final PreSignedUrlService preSignedUrlService;
    private final ExternalPodService externalPodService;
    private final PodQueryService podQueryService;
    private final UserService userService;
    private final OsOptionService osOptionService;


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
                    ? HttpStatus.BAD_REQUEST
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
            log.info("[pre-sign] pod={} ns={} ingress={}",
                    sel.getPodName(), sel.getPodNamespace(), sel.getIngressUrl());

            // Service 내부에서 sel.getPodName(), sel.getPodNamespace(), sel.getIngressUrl()만 사용
            PreSignedUrlResponseDto dto =
                    preSignedUrlService.generateForExistingContainer(sel, email);

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
    @Operation(
            summary = "등급별 생성 가능 OS/버전 목록 조회",
            description = """
                    현재 로그인한 사용자의 등급(BASIC / PRO / ENTERPRISE / ADMIN)에 따라
                    생성 가능한 OS와 버전 목록을 반환합니다.
                    프론트에서는 이 응답을 기반으로 OS 선택 드롭다운을 구성하면 됩니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요 (JWT 없음 또는 만료)")
    })
    @ApiResponse(responseCode = "200", description = "성공적으로 OS 옵션 목록을 반환함")
    @GetMapping("/os-options")
    public ResponseEntity<List<OsOptionResponseDto>> getOsOptions(Authentication auth) {
        return withAuth(auth, email -> {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            List<OsOptionResponseDto> body = osOptionService.getOptionsForUser(user);
            return ResponseEntity.ok(body);
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