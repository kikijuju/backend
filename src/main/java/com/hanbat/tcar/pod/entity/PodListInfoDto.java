package com.hanbat.tcar.pod.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodListInfoDto {
    private String podName;
    private String namespace;
    private String status;
    private String ingressUrl;
    private String calledName;
    private String accessType;
}
