package com.hanbat.tcar.pod.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreSignedUrlResponseDto {
    private String preSignedUrl;
    private String message;
}
