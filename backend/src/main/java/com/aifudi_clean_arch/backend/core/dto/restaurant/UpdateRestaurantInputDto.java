package com.aifudi_clean_arch.backend.core.dto.restaurant;

import java.util.Optional;
import java.util.UUID;

public record UpdateRestaurantInputDto(
        UUID id,
        Optional<String> name,
        Optional<String> address,
        Optional<String> openingTime,
        Optional<UUID> kitchenId
) {
}
