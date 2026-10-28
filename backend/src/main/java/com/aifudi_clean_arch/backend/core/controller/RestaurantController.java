package com.aifudi_clean_arch.backend.core.controller;

import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.*;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class RestaurantController {
    private final CreateRestaurantUseCase createRestaurantUseCase;
    private final UpdateRestaurantUseCase updateRestaurantUseCase;
    private final DeleteRestaurantUseCase deleteRestaurantUseCase;
    private final GetRestaurantUseCase getRestaurantUseCase;

    public CreateRestaurantOutputDto createRestaurant(UUID authUserId, CreateRestaurantInputDto inputDto) {
        return createRestaurantUseCase.createRestaurant(authUserId, inputDto);
    }

    public UpdateRestaurantOutputDto updateRestaurant(UUID authUserId, UpdateRestaurantInputDto inputDto) {
        return updateRestaurantUseCase.updateRestaurant(authUserId, inputDto);
    }

    public void deleteRestaurant(UUID authUserId,UUID restaurantId) {
        deleteRestaurantUseCase.deleteRestaurant(authUserId ,restaurantId);
    }

    public GetRestaurantOutputDto getRestaurant(UUID authUserId ,UUID restaurantId) {
        return getRestaurantUseCase.getRestaurant(authUserId, restaurantId);
    }
}
