package com.aifudi_clean_arch.backend.core.usecase.menuItem;

import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class DeleteMenuItemUseCase {
    private final MenuItemGateway menuItemGateway;
    private final CheckMenuItemPermission checkMenuItemPermission;

    public void deleteMenuItem(UUID authId ,UUID menuItemId) {
        checkMenuItemPermission.checkDeleteMenuItem(authId, menuItemId);

        var isDeleted = menuItemGateway.delete(menuItemId);
        if(!isDeleted){
            throw new MenuItemNotFountException(menuItemId);
        }
    }
}
