package com.aifudi_clean_arch.backend.core.usecase.restaurant;

import com.aifudi_clean_arch.backend.core.domain.Membership;
import com.aifudi_clean_arch.backend.core.domain.Restaurant;
import com.aifudi_clean_arch.backend.core.dto.restaurant.CreateRestaurantInputDto;
import com.aifudi_clean_arch.backend.core.dto.restaurant.CreateRestaurantOutputDto;
import com.aifudi_clean_arch.backend.core.exception.KitchenNotFoundException;
import com.aifudi_clean_arch.backend.core.exception.SubRoleNotFoundException;
import com.aifudi_clean_arch.backend.core.exception.UserNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.*;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class CreateRestaurantUseCase {
    private final RestaurantGateway restaurantGateway;
    private final UserGateway userGateway;
    private final SubRoleGateway subRoleGateway;
    private final KitchenGateway kitchenGateway;
    private final MembershipGateway membershipGateway;
    private final TransactionGateway transactionGateway;
    private final CheckRestaurantPermission checkRestaurantPermission;

    public CreateRestaurantOutputDto createRestaurant(UUID authUserId,CreateRestaurantInputDto inputDto) {
        checkRestaurantPermission.checkCreateRestaurant(authUserId);

        var foundUser = userGateway.getUserById(inputDto.ownerId())
                .orElseThrow(() -> new UserNotFoundException(inputDto.ownerId()));

        var foundKitchen = kitchenGateway.getKitchenById(inputDto.kitchenId())
                .orElseThrow(() -> new KitchenNotFoundException(inputDto.kitchenId()));

        var ownerSubRole = subRoleGateway.getByValue("mainOwner")
                .orElseThrow(() -> new SubRoleNotFoundException("mainOwner"));


        var newRestaurant = new Restaurant(
                null,
                inputDto.name(),
                inputDto.address(),
                inputDto.openingTime(),
                foundKitchen,
                Set.of(),
                Set.of()
        );

        return transactionGateway.execute(() -> {
            var savedRestaurant = restaurantGateway.save(newRestaurant);
            var membership = new Membership(null, foundUser, ownerSubRole, savedRestaurant);
            membershipGateway.save(membership);

            return new CreateRestaurantOutputDto(
                    savedRestaurant.getId(),
                    savedRestaurant.getName(),
                    savedRestaurant.getAddress(),
                    savedRestaurant.getOpeningTime(),
                    savedRestaurant.getKitchen().getId(),
                    foundUser.getId()
            );
        });
    }
}
