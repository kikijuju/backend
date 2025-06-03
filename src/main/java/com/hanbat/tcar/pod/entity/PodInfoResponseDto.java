package com.hanbat.tcar.pod.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodInfoResponseDto {
    private String podName;
    private String podNamespace;
    private String ingress;
}
