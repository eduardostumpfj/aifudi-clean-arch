package com.aifudi_clean_arch.backend.core.usecase.restaurant;

import com.aifudi_clean_arch.backend.core.exception.RestaurantNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class DeleteRestaurantUseCase {
    private final RestaurantGateway restaurantGateway;
    private final CheckRestaurantPermission checkRestaurantPermission;

    public void deleteRestaurant(UUID authUserId,UUID restaurantId) {
        checkRestaurantPermission.checkDeleteRestaurant(authUserId, restaurantId);

       var isDeleted = restaurantGateway.delete(restaurantId);
       if(!isDeleted){
           throw new RestaurantNotFoundException(restaurantId);
       }
    }
}
