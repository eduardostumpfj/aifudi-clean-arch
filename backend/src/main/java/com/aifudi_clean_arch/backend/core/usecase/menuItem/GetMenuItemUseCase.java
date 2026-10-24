package com.aifudi_clean_arch.backend.core.usecase.menuItem;

import com.aifudi_clean_arch.backend.core.dto.menuItem.MenuItemOutputDto;
import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class GetMenuItemUseCase {
    private final MenuItemGateway menuItemGateway;

    public MenuItemOutputDto getMenuItemById(UUID id) {
        var foundItem = menuItemGateway.getById(id)
                .orElseThrow(() -> new MenuItemNotFountException(id));

        return new MenuItemOutputDto(
                foundItem.getId(),
                foundItem.getName(),
                foundItem.getDescription(),
                foundItem.getPrice(),
                foundItem.isHasDelivery(),
                foundItem.getPicture(),
                foundItem.getRestaurantId()
        );
    }


}
