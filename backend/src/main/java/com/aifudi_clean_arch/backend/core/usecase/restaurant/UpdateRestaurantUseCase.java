package com.aifudi_clean_arch.backend.core.usecase.restaurant;

import com.aifudi_clean_arch.backend.core.domain.Restaurant;
import com.aifudi_clean_arch.backend.core.dto.restaurant.UpdateRestaurantInputDto;
import com.aifudi_clean_arch.backend.core.dto.restaurant.UpdateRestaurantOutputDto;
import com.aifudi_clean_arch.backend.core.exception.KitchenNotFoundException;
import com.aifudi_clean_arch.backend.core.exception.RestaurantNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.KitchenGateway;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UpdateRestaurantUseCase {
    private final RestaurantGateway restaurantGateway;
    private final KitchenGateway kitchenGateway;

    public UpdateRestaurantOutputDto updateRestaurant(UpdateRestaurantInputDto inputDto) {
        var current = restaurantGateway.getRestaurantById(inputDto.id())
                .orElseThrow(() -> new RestaurantNotFoundException(inputDto.id()));

        var kitchen = inputDto.kitchenId()
                .map(kitchenId -> kitchenGateway.getKitchenById(kitchenId)
                        .orElseThrow(() -> new KitchenNotFoundException(kitchenId)))
                .orElseGet(current::getKitchen);

        var updated = new Restaurant(
                current.getId(),
                inputDto.name().orElse(current.getName()),
                inputDto.address().orElse(current.getAddress()),
                inputDto.openingTime().orElse(current.getOpeningTime()),
                kitchen,
                current.getMembers(),
                current.getMenuItems()
        );

        var saved = restaurantGateway.update(updated);

        return new UpdateRestaurantOutputDto(
                saved.getId(),
                saved.getName(),
                saved.getAddress(),
                saved.getOpeningTime(),
                saved.getKitchen().getId()
        );
    }
}
