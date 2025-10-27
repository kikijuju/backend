package com.hanbat.tcar.account.dto;

import com.hanbat.tcar.user.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import java.time.LocalDate;

@Getter
public class AccountUpdateRequestDto {
    @NotBlank
    private String nickname;
    @NotBlank
    private String username;
    private LocalDate birthDate;
    private Gender gender;
}