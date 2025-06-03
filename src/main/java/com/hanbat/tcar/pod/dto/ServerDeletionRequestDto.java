package com.hanbat.tcar.pod.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerDeletionRequestDto {
    private String podNamespace;
    private String podName;
}