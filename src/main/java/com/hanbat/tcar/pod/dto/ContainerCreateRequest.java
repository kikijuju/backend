package com.hanbat.tcar.pod.dto;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ContainerCreateRequest {
    private final String os;
    private final String version;
    private final String calledName;   // = serverName
    private final String userEmail;    // 내부에서 세팅
}
