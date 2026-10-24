package com.aifudi_clean_arch.backend.core.dto.membership;

import java.util.UUID;

public record MembershipOutputDto(
        UUID id,
        UUID userId,
        UUID subRoleId,
        UUID restaurantId
) {
}
