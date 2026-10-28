package com.aifudi_clean_arch.backend.infra.config;

import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.permission.RestaurantPermissionChecker;
import com.aifudi_clean_arch.backend.core.usecase.permission.MenuItemPermissionChecker;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.CheckMenuItemPermission;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CheckSubRolePermission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermissionDependencyInjectionConfig {
    @Bean
    public RestaurantPermissionChecker permissionChecker(UserGateway users, MembershipGateway memberships) {
        return new RestaurantPermissionChecker(users, memberships);
    }

    @Bean
    public MenuItemPermissionChecker menuItemPermissionChecker(RestaurantPermissionChecker checker, MenuItemGateway items) {
        return new MenuItemPermissionChecker(checker, items);
    }

    @Bean
    public CheckMenuItemPermission checkMenuItemPermission(MenuItemPermissionChecker checker) {
        return new CheckMenuItemPermission(checker);
    }

    @Bean
    public CheckSubRolePermission checkSubRolePermission(RestaurantPermissionChecker checker) {
        return new CheckSubRolePermission(checker);
    }
}
