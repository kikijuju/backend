package com.hanbat.tcar.pod.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodSelectionRequestDto {
    private String podNamespace;
    private String podName;

    @JsonProperty("ingressURL")
    private String ingressUrl;

    // 프론트에서 오는 추가 필드들 (여기선 사용 안 함)
    @JsonProperty("OS")
    private String os;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("Created")
    private String created;

    @JsonProperty("ServerName")
    private String serverName;
}