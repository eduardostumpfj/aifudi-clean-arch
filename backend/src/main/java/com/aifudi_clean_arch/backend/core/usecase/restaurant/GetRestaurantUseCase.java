package com.aifudi_clean_arch.backend.core.usecase.restaurant;

import com.aifudi_clean_arch.backend.core.domain.Membership;
import com.aifudi_clean_arch.backend.core.domain.MenuItem;
import com.aifudi_clean_arch.backend.core.dto.restaurant.GetRestaurantOutputDto;
import com.aifudi_clean_arch.backend.core.exception.RestaurantNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class GetRestaurantUseCase {
    private final RestaurantGateway restaurantGateway;
    private final CheckRestaurantPermission checkRestaurantPermission;

    public GetRestaurantOutputDto getRestaurant(UUID authUserId, UUID restaurantId) {
        checkRestaurantPermission.checkGetRestaurant(authUserId);

        var foundRestaurant = restaurantGateway.getRestaurantById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        Set<UUID> membersId = foundRestaurant.getMembers().stream()
                .map(Membership::getId)
                .collect(Collectors.toSet());

        Set<UUID> itemsId = foundRestaurant.getMenuItems().stream()
                .map(MenuItem::getId)
                .collect(Collectors.toSet());

        return new GetRestaurantOutputDto(
                foundRestaurant.getId(),
                foundRestaurant.getName(),
                foundRestaurant.getAddress(),
                foundRestaurant.getOpeningTime(),
                foundRestaurant.getKitchen().getId(),
                membersId,
                itemsId
        );
    }
}
