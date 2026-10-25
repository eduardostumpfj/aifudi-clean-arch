package com.aifudi_clean_arch.backend.core.usecase.membership;

import com.aifudi_clean_arch.backend.core.domain.Membership;
import com.aifudi_clean_arch.backend.core.exception.ProtectedSubRoleException;
import com.aifudi_clean_arch.backend.core.dto.membership.MembershipInputDto;
import com.aifudi_clean_arch.backend.core.dto.membership.MembershipOutputDto;
import com.aifudi_clean_arch.backend.core.exception.RestaurantNotFoundException;
import com.aifudi_clean_arch.backend.core.exception.SubRoleNotFoundException;
import com.aifudi_clean_arch.backend.core.exception.UserNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.MembershipGateway;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import com.aifudi_clean_arch.backend.core.gateway.UserGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class AssignSubRoleToUserUseCase {
    private final MembershipGateway membershipGateway;
    private final SubRoleGateway subRoleGateway;
    private final RestaurantGateway restaurantGateway;
    private final UserGateway userGateway;

    public MembershipOutputDto assignSubRoleToUser(MembershipInputDto inputDto) {
        var user = userGateway.getUserById(inputDto.userId())
                .orElseThrow(() -> new UserNotFoundException(inputDto.userId()));

        var restaurant = restaurantGateway.getRestaurantById(inputDto.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(inputDto.restaurantId()));

        var subRole = subRoleGateway.getById(inputDto.subRoleid())
                .orElseThrow(() -> new SubRoleNotFoundException(inputDto.subRoleid()));

        var foundMembership = membershipGateway.getUserRestaurantMembership(
                inputDto.userId(), inputDto.restaurantId());

        if (subRole.isMainOwner()) {
            throw new ProtectedSubRoleException(subRole.getId());
        }
        foundMembership.ifPresent(existing -> {
            if (existing.getSubrole().isMainOwner()) {
                throw new ProtectedSubRoleException(existing.getSubrole().getId());
            }
        });

        var id = foundMembership
                .map(Membership::getId)
                .orElseGet(UUID::randomUUID);

        var membership = new Membership(id, user, subRole, restaurant);
        membershipGateway.save(membership);

        return new MembershipOutputDto(
                membership.getId(),
                membership.getUser().getId(),
                membership.getSubrole().getId(),
                membership.getRestaurant().getId()
        );
    }
}
