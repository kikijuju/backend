package com.hanbat.tcar.pod.service;

import com.hanbat.tcar.pod.dto.OsOptionResponseDto;
import com.hanbat.tcar.user.entity.User;
import com.hanbat.tcar.user.entity.UserTier;
import com.hanbat.tcar.user.service.UserTierPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OsOptionService {


    private final UserTierPolicyService userTierPolicyService;

    public List<OsOptionResponseDto> getOptionsForUser(User user) {
        UserTier tier = user.getTier();

        return userTierPolicyService.getAllowedOsOptions(tier).stream()
                .map(opt -> new OsOptionResponseDto(
                        opt.getOs(),
                        opt.getVersions()
                ))
                .collect(Collectors.toList());
    }
}
