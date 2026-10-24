package com.aifudi_clean_arch.backend.core.dto.membership;

import java.util.UUID;

public record MembershipInputDto(
        UUID userId,
        UUID subRoleid,
        UUID restaurantId

) {
}
