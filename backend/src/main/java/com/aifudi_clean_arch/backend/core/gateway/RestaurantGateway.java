package com.aifudi_clean_arch.backend.core.gateway;

import com.aifudi_clean_arch.backend.core.domain.Restaurant;

import java.util.Optional;
import java.util.UUID;

public interface RestaurantGateway {
    Optional<Restaurant> getRestaurantById(UUID id);
    Restaurant save(Restaurant restaurant);
    boolean delete(UUID id);
    Restaurant update(Restaurant restaurant);
}
