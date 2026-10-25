package com.aifudi_clean_arch.backend.infra.database.mapper;

import com.aifudi_clean_arch.backend.core.domain.MenuItem;
import com.aifudi_clean_arch.backend.core.domain.Restaurant;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.MenuItemEntity;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.RestaurantEntity;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper {
    public MenuItem map(MenuItemEntity entity) {
        return new MenuItem(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.isHas_delivery(),
                entity.getPicture(),
                entity.getRestaurant().getId()
        );
    }

    public MenuItemEntity map(MenuItem menuItem, RestaurantEntity restaurant) {
        return new MenuItemEntity(
                menuItem.getId(),
                restaurant,
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getPicture(),
                menuItem.isHasDelivery()
        );
    }
}
