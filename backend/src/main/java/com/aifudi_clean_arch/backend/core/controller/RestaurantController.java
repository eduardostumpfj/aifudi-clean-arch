package com.aifudi_clean_arch.backend.core.controller;

import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.CreateRestaurantUseCase;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.DeleteRestaurantUseCase;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.GetRestaurantUseCase;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.UpdateRestaurantUseCase;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RestaurantController {
    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final UpdateRestaurantUseCase updateRestaurantUseCase;
    private final DeleteRestaurantUseCase deleteRestaurantUseCase;
    private final GetRestaurantUseCase getRestaurantUseCase;

    public CreateRestaurantOutputDto createRestaurant(CreateRestaurantInputDto inputDto) {
        return createRestaurantUseCase.createRestaurant(inputDto);
    }

    public UpdateRestaurantOutputDto updateRestaurant(UpdateRestaurantInputDto inputDto) {
        return updateRestaurantUseCase.updateRestaurant(inputDto);
    }

    public void deleteRestaurant(UUID restaurantId) {
        deleteRestaurantUseCase.deleteRestaurant(restaurantId);
    }

    public GetRestaurantOutputDto getRestaurant(UUID restaurantId) {
        return getRestaurantUseCase.getRestaurant(restaurantId);
    }
}
