package com.aifudi_clean_arch.backend.core.dto.restaurant;


import java.util.Set;
import java.util.UUID;

public record GetRestaurantOutputDto(
        UUID id,
        String name,
        String address,
        String openingTime,
        UUID kitchenId,
        Set<UUID> membershipIds,
        Set<UUID> menuItemsIdList

) {

}
