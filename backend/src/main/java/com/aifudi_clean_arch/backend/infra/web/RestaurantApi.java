package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.RestaurantController;
import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.infra.web.json.UpdateRestaurantJson;
import com.aifudi_clean_arch.backend.infra.web.json.CreateRestaurantJson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("v1/restaurants")
@RequiredArgsConstructor
public class RestaurantApi {
    private final RestaurantController restaurantController;
    private final BasicHeaderAuthenticator authenticator;

    @PostMapping("/register")
    public ResponseEntity<CreateRestaurantOutputDto> createRestaurant(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateRestaurantJson input
    ) {
        var userId = authenticator.authenticate(authHeader);
        var created = restaurantController.createRestaurant(userId, new CreateRestaurantInputDto(
                input.ownerId(), input.name(), input.address(), input.openingTime(), input.kitchenId()));
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateRestaurantOutputDto> updateRestaurant(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody UpdateRestaurantJson input
    ){
        var userId = authenticator.authenticate(authHeader);
        var updated = restaurantController.updateRestaurant(userId, new UpdateRestaurantInputDto(
                id, Optional.ofNullable(input.name()), Optional.ofNullable(input.address()),
                Optional.ofNullable(input.openingTime()), Optional.ofNullable(input.kitchenId())));
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetRestaurantOutputDto> getRestaurant(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        var userId = authenticator.authenticate(authHeader);
        var restaurant = restaurantController.getRestaurant(userId, id);
        return ResponseEntity.ok(restaurant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        var userId = authenticator.authenticate(authHeader);
        restaurantController.deleteRestaurant(userId, id);

        return ResponseEntity.ok().build();
    }

}
