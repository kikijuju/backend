package com.hanbat.tcar.pod.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class OsOptionResponseDto {
    private String os;
    private List<String> versions;
}
