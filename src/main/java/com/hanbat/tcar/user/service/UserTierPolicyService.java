package com.hanbat.tcar.user.service;

import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.entity.UserTier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserTierPolicyService {

    @Getter
    @AllArgsConstructor
    public static class OsOption {
        private String os;              // "ubuntu", "debian" ...
        private List<String> versions;  // ["22.04", "24.04"] ...
    }

    // 등급별 최대 서버 개수
    public int getMaxServers(UserTier tier) {
        return switch (tier) {
            case BASIC -> 1;
            case PRO -> 5;
            case ENTERPRISE -> 10;
            case ADMIN -> Integer.MAX_VALUE; // 사실상 무제한
        };
    }

    // 등급별 허용 OS/버전 목록
    public List<OsOption> getAllowedOsOptions(UserTier tier) {
        return switch (tier) {
            case BASIC -> List.of(
                    new OsOption("ubuntu", List.of("22.04", "24.04"))
            );
            case PRO -> List.of(
                    new OsOption("ubuntu", List.of("18.04", "20.04", "22.04", "24.04")),
                    new OsOption("debian", List.of("latest", "bookworm", "bullseye", "buster")),
                    new OsOption("rockylinux", List.of("8", "9"))
            );
            case ENTERPRISE -> List.of(
                    new OsOption("ubuntu", List.of("18.04", "20.04", "22.04", "24.04")),
                    new OsOption("debian", List.of("latest", "bookworm", "bullseye", "buster")),
                    new OsOption("rockylinux", List.of("8", "9")),
                    new OsOption("fedora", List.of("38", "39", "40")),
                    new OsOption("amazonlinux", List.of("latest", "2", "2023"))
            );
            case ADMIN -> List.of(
                    // ADMIN은 그냥 “전부 허용”이라 ENTERPRISE와 같게 두고,
                    // 필요하면 나중에 더 추가
                    new OsOption("ubuntu", List.of("18.04", "20.04", "22.04", "24.04")),
                    new OsOption("debian", List.of("latest", "bookworm", "bullseye", "buster")),
                    new OsOption("rockylinux", List.of("8", "9")),
                    new OsOption("fedora", List.of("38", "39", "40")),
                    new OsOption("amazonlinux", List.of("latest", "2", "2023"))
            );
        };
    }

    public boolean canCreateNewPod(User user, int currentCount) {
        int max = getMaxServers(user.getTier());
        return currentCount < max;   // 최대보다 작아야 생성 가능
    }


    // 특정 OS/버전이 해당 등급에서 허용되는지 체크
    public boolean isAllowed(UserTier tier, String os, String version) {
        return getAllowedOsOptions(tier).stream()
                .filter(opt -> opt.getOs().equalsIgnoreCase(os))
                .anyMatch(opt -> opt.getVersions().contains(version));
    }

    // PreSignedUrlService에서 호출하는 형태로 오버로드
    public boolean isOsAllowed(User user, String os, String version) {
        return isAllowed(user.getTier(), os, version);
    }


}
