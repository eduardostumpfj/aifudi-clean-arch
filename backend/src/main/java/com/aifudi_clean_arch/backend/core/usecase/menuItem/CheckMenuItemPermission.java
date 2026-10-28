package com.aifudi_clean_arch.backend.core.usecase.menuItem;

import com.aifudi_clean_arch.backend.core.usecase.permission.MenuItemPermissionChecker;
import lombok.AllArgsConstructor;
import java.util.UUID;

@AllArgsConstructor
public class CheckMenuItemPermission {
    private final MenuItemPermissionChecker permissionChecker;

    public void checkCreateMenuItem(UUID userId, UUID restaurantId) {
        permissionChecker.checkCreateMenuItem(userId, restaurantId);
    }

    public void checkUpdateMenuItem(UUID userId, UUID menuItemId) {
        permissionChecker.checkManageMenuItem(userId, menuItemId);
    }

    public void checkDeleteMenuItem(UUID userId, UUID menuItemId) {
        permissionChecker.checkManageMenuItem(userId, menuItemId);
    }

    public void checkGetMenuItem(UUID userId) {
        permissionChecker.checkGetMenuItem(userId);
    }
}
