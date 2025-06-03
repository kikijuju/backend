package com.hanbat.tcar.pod.service;


import com.hanbat.tcar.pod.entity.PodInfoResponseDto;
import com.hanbat.tcar.pod.entity.PodListInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

@Service
public class ExternalPodService {

    @Value("${external.container.ip}")
    private String containerIp;

    // Pod 생성 요청
    public PodInfoResponseDto getContainerInfo(String os, String version, String userEmail) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("os", os);
        requestBody.put("version", version);
        requestBody.put("userEmail", userEmail);

        String externalUrl = "http://" + containerIp + ":8080/api/pod/create";

        return restTemplate.postForObject(externalUrl, requestBody, PodInfoResponseDto.class);
    }

    // Pod 삭제 요청
    public boolean deleteContainer(String userEmail, String podNamespace, String podName) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userEmail", userEmail);
        requestBody.put("podNamespace", podNamespace);
        requestBody.put("podName", podName);

        String externalUrl = "http://" + containerIp + ":8080/api/pod/delete";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Void> response = restTemplate.exchange(externalUrl, HttpMethod.DELETE, entity, Void.class);
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Pod 목록 조회 요청
    public List<PodListInfoDto> getUserPodList(String userEmail) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://" + containerIp + ":8080/api/access/pods?userEmail=" + userEmail;

        ResponseEntity<PodListInfoDto[]> response =
                restTemplate.getForEntity(url, PodListInfoDto[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return Arrays.asList(response.getBody());
        } else {
            return Collections.emptyList();
        }
    }
}

