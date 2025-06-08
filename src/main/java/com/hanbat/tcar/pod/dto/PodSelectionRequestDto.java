package com.hanbat.tcar.pod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  프론트엔드가 사용자가 클릭한 컨테이너 정보를 담아서 보내는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodSelectionRequestDto {
    private String podNamespace;   // 사용자가 클릭한 Pod이 속한 네임스페이스
    private String podName;        // 사용자가 클릭한 Pod 이름
    private String ingressUrl;     // 해당 Pod에 접근하기 위한 Ingress 주소

}
