package com.hanbat.tcar.pod.service;

import com.hanbat.tcar.pod.dto.ContainerCreateRequest;
import com.hanbat.tcar.pod.dto.ContainerDeleteRequest;
import com.hanbat.tcar.pod.entity.PodInfo;
import com.hanbat.tcar.pod.entity.PodListInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalPodService {

    /** 애플리케이션 전체에서 하나의 RestTemplate 빈을 공유하도록 DI */
    private final RestTemplate restTemplate;

    /** 가상 환경 서버의 IP (yml - external.container.ip) */
    @Value("${external.container.ip}")
    private String containerIp;

    /* ─────────────────────────────────────────────
     *  1) Pod 생성
     * ───────────────────────────────────────────── */
    public Optional<PodInfo> createContainer(ContainerCreateRequest req) {

        String url = baseUrl() + "/pod/create";

        try {
            ResponseEntity<PodInfo> response =
                    restTemplate.postForEntity(url, req, PodInfo.class);

            return response.getStatusCode().is2xxSuccessful()
                    ? Optional.ofNullable(response.getBody())
                    : Optional.empty();

        } catch (Exception e) {
            log.error("Pod 생성 실패", e);
            return Optional.empty();
        }
    }

    /* ─────────────────────────────────────────────
     *  Pod 삭제
     *  - 요청 바디: { "podNamespace": "...", "podName": "..." }
     *  - userEmail은 보내지 않음
     * ───────────────────────────────────────────── */
    public boolean deleteContainer(ContainerDeleteRequest req) {

        String url = baseUrl() + "/pod/delete";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ContainerDeleteRequest> entity = new HttpEntity<>(req, headers);

        try {
            ResponseEntity<Void> res =
                    restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            return res.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Pod 삭제 실패", e);
            return false;
        }
    }

    /* ─────────────────────────────────────────────
     *  3) 사용자 Pod 목록 조회
     * ───────────────────────────────────────────── */
    public List<PodListInfoDto> fetchUserPods(String userEmail) {

        String url = baseUrl() + "/access/pods?userEmail=" + userEmail;

        try {
            ResponseEntity<List<PodListInfoDto>> res =
                    restTemplate.exchange(url,
                            HttpMethod.GET,
                            HttpEntity.EMPTY,
                            new ParameterizedTypeReference<>() {});

            return res.getStatusCode().is2xxSuccessful() && res.getBody() != null
                    ? res.getBody()
                    : Collections.emptyList();

        } catch (Exception e) {
            log.error("Pod 목록 조회 실패", e);
            return Collections.emptyList();
        }
    }

    /* ─────────────────────────────────────────────
     *  공통: 베이스 URL 조립
     * ───────────────────────────────────────────── */
    private String baseUrl() {
        return "http://" + containerIp + ":8080/api";
    }
}