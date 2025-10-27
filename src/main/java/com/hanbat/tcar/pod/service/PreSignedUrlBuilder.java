// PreSignedUrlBuilder.java
package com.hanbat.tcar.pod.service;

import com.hanbat.tcar.config.PreSignedUrlConfig;
import com.hanbat.tcar.pod.entity.PodInfo;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class PreSignedUrlBuilder {

    public String build(PreSignedUrlConfig cfg, String jwt, PodInfo pod) {
        // contextPath는 보통 /api/access/presigned/validate 형태로 설정됨
        return String.format(
                "%s://%s:%d%s?token=%s&podName=%s&podNamespace=%s",
                cfg.getProtocol(),
                cfg.getHost(),
                cfg.getPort(),
                cfg.getContextPath(), // /api/access/presigned/validate
                urlEncode(jwt),
                urlEncode(pod.getPodName()),
                urlEncode(pod.getPodNamespace())
        );
    }

    private String urlEncode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }
}
