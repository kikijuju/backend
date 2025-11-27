// com.hanbat.tcar.user.dto.TierUpdateRequestDto

package com.hanbat.tcar.user.dto;

import com.hanbat.tcar.user.entity.UserTier;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TierUpdateRequestDto {
    private UserTier tier;   // BASIC / PRO / ENTERPRISE / ADMIN
}
