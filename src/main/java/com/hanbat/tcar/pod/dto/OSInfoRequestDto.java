package com.hanbat.tcar.pod.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSInfoRequestDto {
    private String os;
    private String version;
    @JsonProperty("serverName")
    private String serverName;
}
