package com.aifudi_clean_arch.backend.core.usecase.menuItem;

import com.aifudi_clean_arch.backend.core.domain.MenuItem;
import com.aifudi_clean_arch.backend.core.dto.menuItem.MenuItemOutputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.UpdateMenuItemInputDto;
import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.core.exception.RestaurantNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CheckSubRolePermission;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class UpdateMenuItemUseCase {
    private final RestaurantGateway  restaurantGateway;
    private final MenuItemGateway menuItemGateway;
    private final CheckMenuItemPermission permission;

    public MenuItemOutputDto updateMenuItem(UUID authId,UpdateMenuItemInputDto inputDto) {
        permission.checkUpdateMenuItem(authId, inputDto.id());

        var current = menuItemGateway.getById(inputDto.id())
                .orElseThrow(() -> new MenuItemNotFountException(inputDto.id()));

        UUID newRestaurantId;
        if (inputDto.restaurantId().isPresent()) {
            var restaurantId = inputDto.restaurantId().get();

            var foundRestaurant = restaurantGateway.getRestaurantById(restaurantId)
                    .orElseThrow(() ->
                            new RestaurantNotFoundException(restaurantId));
            newRestaurantId = foundRestaurant.getId();
        } else {
            newRestaurantId = current.getRestaurantId();
        }
        var updated = new MenuItem(
                inputDto.id(),
                inputDto.name().orElse(current.getName()),
                inputDto.description().orElse(current.getDescription()),
                inputDto.price().orElse(current.getPrice()),
                inputDto.has_delivery().orElse(current.isHasDelivery()),
                inputDto.picture().orElse(current.getPicture()),
                newRestaurantId
        );

        var updatedMenuItem = menuItemGateway.update(updated);

        return new MenuItemOutputDto(
                updatedMenuItem.getId(),
                updatedMenuItem.getName(),
                updatedMenuItem.getDescription(),
                updatedMenuItem.getPrice(),
                updatedMenuItem.isHasDelivery(),
                updatedMenuItem.getPicture(),
                updatedMenuItem.getRestaurantId()
        );
    }

}
