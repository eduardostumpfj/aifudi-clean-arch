package com.aifudi_clean_arch.backend.core.usecase.restaurant;

import com.aifudi_clean_arch.backend.core.usecase.permission.RestaurantPermissionChecker;
import lombok.AllArgsConstructor;
import java.util.UUID;

@AllArgsConstructor
public class CheckRestaurantPermission {
    private final RestaurantPermissionChecker permissionChecker;

    public void checkCreateRestaurant(UUID userId) {
        permissionChecker.checkRoles(userId, "ADMIN", "OWNER");
    }

    public void checkUpdateRestaurant(UUID userId, UUID restaurantId) {
        permissionChecker.checkManageRestaurant(userId, restaurantId);
    }

    public void checkDeleteRestaurant(UUID userId, UUID restaurantId) {
        permissionChecker.checkManageRestaurant(userId, restaurantId);
    }

    public void checkGetRestaurant(UUID userId) {
        permissionChecker.checkRoles(userId, "ADMIN", "OWNER", "USER");
    }
}
