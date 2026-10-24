package com.aifudi_clean_arch.backend.core.dto.subRole;

import java.util.Optional;
import java.util.UUID;

public record UpdateSubRoleInputDto(
        UUID id,
        Optional<String> value,
        Optional<String> label,
        Optional<UUID> roleId
) {
}
