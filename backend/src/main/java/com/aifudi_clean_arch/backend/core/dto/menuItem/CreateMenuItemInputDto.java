package com.aifudi_clean_arch.backend.core.dto.menuItem;

import java.util.UUID;
import java.math.BigDecimal;

public record CreateMenuItemInputDto(
        String name,
        String description,
        BigDecimal price,
        boolean has_delivery,
        String picture,
        UUID restaurantId
) {
}
