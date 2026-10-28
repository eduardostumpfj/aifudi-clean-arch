package com.aifudi_clean_arch.backend.core.usecase.permission;

import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import lombok.AllArgsConstructor;
import java.util.UUID;

@AllArgsConstructor
public class MenuItemPermissionChecker {
    private final RestaurantPermissionChecker restaurantPermissionChecker;
    private final MenuItemGateway menuItemGateway;

    public void checkCreateMenuItem(UUID userId, UUID restaurantId) {
        restaurantPermissionChecker.checkManageRestaurant(userId, restaurantId);
    }

    public void checkManageMenuItem(UUID userId, UUID menuItemId) {
        var item = menuItemGateway.getById(menuItemId)
                .orElseThrow(() -> new MenuItemNotFountException(menuItemId));
        restaurantPermissionChecker.checkManageRestaurant(userId, item.getRestaurantId());
    }

    public void checkGetMenuItem(UUID userId) {
        restaurantPermissionChecker.checkRoles(userId, "ADMIN", "OWNER", "USER");
    }
}
