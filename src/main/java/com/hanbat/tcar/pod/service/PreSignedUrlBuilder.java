package com.hanbat.tcar.pod.service;

import com.hanbat.tcar.config.PreSignedUrlConfig;
import com.hanbat.tcar.pod.entity.PodInfo;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class PreSignedUrlBuilder {
    public String build(PreSignedUrlConfig cfg, String jwt, PodInfo pod) {
        return String.format("%s://%s:%d%s?token=%s&podName=%s&podNamespace=%s",
                cfg.getProtocol(),
                cfg.getHost(),
                cfg.getPort(),
                cfg.getContextPath(),
                urlEncode(jwt),
                urlEncode(pod.getPodName()),
                urlEncode(pod.getPodNamespace()));
    }

    private String urlEncode(String raw) {
        try {
            return URLEncoder.encode(raw, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to URL-encode: " + raw, e);
        }
    }

}
