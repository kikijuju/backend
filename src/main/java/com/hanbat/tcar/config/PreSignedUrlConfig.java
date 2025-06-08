package com.hanbat.tcar.config;

import org.springframework.stereotype.Component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Component
@ConfigurationProperties(prefix = "pre-signed")
public class PreSignedUrlConfig {
    private String protocol;
    private String host;
    private String port;
    private String contextPath;
}