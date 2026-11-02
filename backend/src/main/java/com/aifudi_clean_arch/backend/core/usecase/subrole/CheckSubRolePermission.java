package com.aifudi_clean_arch.backend.core.usecase.subrole;

import com.aifudi_clean_arch.backend.core.usecase.permission.RestaurantPermissionChecker;
import lombok.AllArgsConstructor;
import java.util.UUID;

@AllArgsConstructor
public class CheckSubRolePermission {
    private final RestaurantPermissionChecker permissionChecker;

    public void checkCreateSubRole(UUID userId) {
        permissionChecker.checkRoles(userId, "ADMIN");
    }

    public void checkUpdateSubRole(UUID userId) {
        permissionChecker.checkRoles(userId, "ADMIN");
    }

    public void checkDeleteSubRole(UUID userId) {
        permissionChecker.checkRoles(userId, "ADMIN");
    }

    public void checkGetSubRole(UUID userId) {
        permissionChecker.checkRoles(userId, "ADMIN");
    }

    public boolean checkAssignSubRole(UUID userId, UUID restaurantId) {
        return permissionChecker.checkAssignSubRole(userId, restaurantId);
    }
}
