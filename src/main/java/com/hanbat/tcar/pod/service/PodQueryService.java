package com.hanbat.tcar.pod.service;

import com.hanbat.tcar.pod.dto.PodListResponseDto;
import com.hanbat.tcar.pod.entity.PodListInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PodQueryService {

    private final ExternalPodService externalPodService;

    /** 가상 서버 → 프런트 DTO 변환 */
    public List<PodListResponseDto> getPodsForFront(String userEmail) {
        List<PodListInfoDto> extern = externalPodService.fetchUserPods(userEmail);

        return extern.stream()
                .map(p -> new PodListResponseDto(
                        p.getPodName(),
                        p.getNamespace(),
                        p.getStatus(),
                        p.getIngressUrl(),
                        p.getCalledName(),      // ← serverName 으로 매핑
                        p.getAccessType()))
                .collect(Collectors.toList());
    }
}