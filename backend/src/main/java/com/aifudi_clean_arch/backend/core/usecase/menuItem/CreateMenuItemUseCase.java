package com.aifudi_clean_arch.backend.core.usecase.menuItem;

import com.aifudi_clean_arch.backend.core.domain.MenuItem;
import com.aifudi_clean_arch.backend.core.dto.menuItem.CreateMenuItemInputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.MenuItemOutputDto;
import com.aifudi_clean_arch.backend.core.exception.RestaurantNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CreateMenuItemUseCase {
    private final RestaurantGateway  restaurantGateway;
    private final MenuItemGateway menuItemGateway;
    private final CheckMenuItemPermission checkMenuItemPermission;

    public MenuItemOutputDto createMenuItem(UUID authUserId, CreateMenuItemInputDto inputDto){
        checkMenuItemPermission.checkCreateMenuItem(authUserId, inputDto.restaurantId());

        var foundRestaurant = restaurantGateway.getRestaurantById(inputDto.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(inputDto.restaurantId()));

        var newMenuItem = new MenuItem(
                null,
                inputDto.name(),
                inputDto.description(),
                inputDto.price(),
                inputDto.has_delivery(),
                inputDto.picture(),
                inputDto.restaurantId()
        );

        var savedMenuItem = menuItemGateway.save(newMenuItem);

        return new MenuItemOutputDto(
                savedMenuItem.getId(),
                savedMenuItem.getName(),
                savedMenuItem.getDescription(),
                savedMenuItem.getPrice(),
                savedMenuItem.isHasDelivery(),
                savedMenuItem.getPicture(),
                foundRestaurant.getId()
        );
    }

}
