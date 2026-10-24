package com.aifudi_clean_arch.backend.core.dto.restaurant;

import java.util.UUID;

public record CreateRestaurantInputDto(
    UUID ownerId,
    String name,
    String address,
    String openingTime,
    UUID kitchenId
) {
}
