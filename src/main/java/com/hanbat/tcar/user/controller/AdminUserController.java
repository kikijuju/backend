package com.hanbat.tcar.user.controller;

import com.hanbat.tcar.user.dto.TierUpdateRequestDto;
import com.hanbat.tcar.user.service.UserService;
import com.hanbat.tcar.user.dto.SimpleMessageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor

@Tag(
        name = "Admin - User Management",
        description = "관리자가 사용자 정보를 관리하는 API (티어 수정 등)"
)
public class AdminUserController {

    private final UserService userService;


    @Operation(
            summary = "사용자 등급(Tier) 변경",
            description = """
                    관리자가 특정 사용자의 등급(BASIC / PRO / ENTERPRISE / ADMIN)을 변경합니다.
                    프론트/백엔드 관리자 페이지에서 사용되며,
                    해당 API는 반드시 관리자 권한에서만 호출 가능해야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "티어 변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 혹은 존재하지 않는 유저"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (JWT 없음/만료)"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
    })
    // 티어 변경: /api/admin/users/{id}/tier
    @PostMapping("/{id}/tier")
    // @PreAuthorize("hasRole('ADMIN')")   // 권한 체계 맞춰서 나중에 활성화
    public ResponseEntity<SimpleMessageResponseDto> updateUserTier(
            @PathVariable Long id,
            @RequestBody TierUpdateRequestDto req
    ) {
        userService.updateTier(id, req.getTier());
        return ResponseEntity.ok(new SimpleMessageResponseDto("Tier updated"));
    }
}
