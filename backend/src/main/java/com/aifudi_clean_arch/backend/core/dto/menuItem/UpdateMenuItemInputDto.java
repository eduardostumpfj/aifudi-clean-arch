package com.aifudi_clean_arch.backend.core.dto.menuItem;

import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

public record UpdateMenuItemInputDto(
        UUID id,
        Optional<String> name,
        Optional<String> description,
        Optional<BigDecimal> price,
        Optional<Boolean> has_delivery,
        Optional<String> picture,
        Optional<UUID> restaurantId
) {
}
