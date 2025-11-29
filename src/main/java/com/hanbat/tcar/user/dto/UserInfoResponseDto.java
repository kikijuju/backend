package com.hanbat.tcar.user.dto;

import com.hanbat.tcar.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponseDto {
    private Long id;
    private String email;
    private String name;
    private String tier;

    public UserInfoResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getUsername();
        this.tier = user.getTier().name();
    }
}

