package com.hanbat.tcar.user.service;


import com.hanbat.tcar.user.dto.ExternalContainerResponse;
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

@Service
public class ExternalContainerService {
    // IP 주소와 경로를 별도로 주입받음
    @Value("${external.container.ip}")
    private String containerIp;
    /**
     * OS, version, userEmail을 받아 외부 서버(192.168.0.5)로 POST 요청을 보내
     * containerId, port 정보를 받아오는 메서드 예시
     */
    // @Value("${external.container.path.delete}")  // 삭제용 경로 설정 (예: /api/container/delete)
    // private String containerDeletePath;


    public ExternalContainerResponse getContainerInfo(String os, String version, String userEmail) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 바디 구성
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("os", os);
        requestBody.put("version", version);
        requestBody.put("userEmail", userEmail);

        // IP 주소와 경로를 조합하여 외부 서버 URL 생성
        String externalUrl = "http://" + containerIp + ":8080/api/container/create";

        ExternalContainerResponse response =
                restTemplate.postForObject(externalUrl, requestBody, ExternalContainerResponse.class);

        return response;
    }

    /**
     * 유저 이메일과 컨테이너 ID를 받아 외부 서버로 DELETE 요청을 보내 컨테이너 삭제 수행
     */
    public boolean deleteContainer(String userEmail, String containerId) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 바디 또는 파라미터 구성 (외부 API 스펙에 맞게 조정)
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("userEmail", userEmail);
        requestBody.put("containerId", containerId);
        System.out.println(requestBody);

        // 외부 서버 삭제 URL 구성
        String externalUrl = "http://" + containerIp + ":8080/api/container/delete";

        // 예시로 POST 방식으로 삭제 요청을 보낸다고 가정 (실제 API 스펙에 맞춰 DELETE 등으로 변경)
        try {
            // 헤더 구성 (JSON)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // HttpEntity에 요청 본문과 헤더 포함
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // exchange() 메서드를 사용하여 DELETE 요청 보내기
            ResponseEntity<Void> response = restTemplate.exchange(externalUrl, HttpMethod.DELETE, entity, Void.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            // 오류 발생 시 false 반환
            e.printStackTrace();
            return false;
        }
    }
    }
