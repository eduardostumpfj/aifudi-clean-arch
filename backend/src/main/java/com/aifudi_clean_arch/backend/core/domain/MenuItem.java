package com.aifudi_clean_arch.backend.core.domain;

import com.aifudi_clean_arch.backend.core.exception.MenuItemInvalidPrice;
import lombok.Getter;

import java.util.UUID;
import java.math.BigDecimal;

@Getter
public class MenuItem {
    private final UUID id;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final boolean hasDelivery;
    private final String picture;
    private final UUID restaurantId;

    public MenuItem(UUID id, String name, String description, BigDecimal price,
                    boolean has_delivery, String picture, UUID restaurantId) {
        if (price == null || price.signum() <= 0) {
            throw new MenuItemInvalidPrice(id);
        }
        if (restaurantId == null) {
            throw new IllegalArgumentException("Restaurante deve ter ID.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome do item é obrigatório.");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.hasDelivery = has_delivery;
        this.picture = picture;
        this.restaurantId = restaurantId;
    }

    public boolean hasPrice(){
        return price.signum() > 0;
    }
}
