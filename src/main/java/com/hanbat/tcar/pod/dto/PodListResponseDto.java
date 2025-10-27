package com.hanbat.tcar.pod.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** 프런트에 돌려주는 Pod 정보 */
@Getter
@AllArgsConstructor
public class PodListResponseDto {
    private String podName;
    private String namespace;
    private String status;
    private String ingressUrl;
    private String serverName;   // ✅ calledName → serverName
    private String accessType;

}
