package com.aifudi_clean_arch.backend.core.dto.subRole;

import java.util.UUID;

public record CreateSubRoleInputDto(
    String value,
    String label,
    UUID roleId
) {
}
