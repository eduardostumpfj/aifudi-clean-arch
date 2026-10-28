package com.aifudi_clean_arch.backend.infra.config;

import com.aifudi_clean_arch.backend.core.controller.MenuItemController;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.*;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.CheckRestaurantPermission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MenuItemDependencyInjectionConfig {
    @Bean
    public CreateMenuItemUseCase createMenuItemUseCase(
        RestaurantGateway restaurantGateway,
        MenuItemGateway menuItemGateway,
        CheckMenuItemPermission checkMenuItemPermission
    ) {
        return new CreateMenuItemUseCase(restaurantGateway, menuItemGateway, checkMenuItemPermission);
    }

    @Bean
    public DeleteMenuItemUseCase deleteMenuItemUseCase(
            MenuItemGateway menuItemGateway,
            CheckMenuItemPermission checkMenuItemPermission
    ){
        return new DeleteMenuItemUseCase(menuItemGateway, checkMenuItemPermission);
    }

    @Bean
    public UpdateMenuItemUseCase updateMenuItemUseCase(
            RestaurantGateway restaurantGateway,
            MenuItemGateway menuItemGateway,
            CheckMenuItemPermission checkMenuItemPermission
    ){
        return new UpdateMenuItemUseCase(restaurantGateway,menuItemGateway, checkMenuItemPermission);
    }

    @Bean
    public GetMenuItemUseCase getMenuItemUseCase(
            MenuItemGateway menuItemGateway,
            CheckMenuItemPermission checkMenuItemPermission
    ) {
        return new  GetMenuItemUseCase(menuItemGateway, checkMenuItemPermission);
    }

    @Bean
    public MenuItemController menuItemController(
            CreateMenuItemUseCase createMenuItemUseCase,
            DeleteMenuItemUseCase deleteMenuItemUseCase,
            UpdateMenuItemUseCase  updateMenuItemUseCase,
            GetMenuItemUseCase  getMenuItemUseCase
    ) {
        return new MenuItemController(createMenuItemUseCase, deleteMenuItemUseCase, updateMenuItemUseCase, getMenuItemUseCase);
    }
}
