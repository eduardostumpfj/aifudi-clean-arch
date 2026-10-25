package com.aifudi_clean_arch.backend.infra.database.mapper;

import lombok.RequiredArgsConstructor;
import com.aifudi_clean_arch.backend.core.domain.Restaurant;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.RestaurantEntity;

import java.util.Set;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantMapper {
    private final KitchenMapper kitchenMapper;

    public Restaurant map(RestaurantEntity entity) {
        return new Restaurant(
                entity.getId(),
                entity.getName(),
                entity.getAddress(),
                entity.getOpening_time(),
                kitchenMapper.map(entity.getKitchen()),
                Set.of(),
                Set.of()
        );
    }

    public RestaurantEntity map(Restaurant entity) {
        return new RestaurantEntity(
                entity.getId(),
                kitchenMapper.map(entity.getKitchen()),
                entity.getName(),
                entity.getAddress(),
                entity.getOpeningTime()
        );
    }

}
