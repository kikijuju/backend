package com.hanbat.tcar.user.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSInfoRequestDto {
    private String os;
    private String version;
}
