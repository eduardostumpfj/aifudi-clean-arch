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
import com.aifudi_clean_arch.backend.core.usecase.subrole.CheckSubRolePermission;

import java.util.UUID;

@AllArgsConstructor
public class AssignSubRoleToUserUseCase {
    private final MembershipGateway membershipGateway;
    private final SubRoleGateway subRoleGateway;
    private final RestaurantGateway restaurantGateway;
    private final UserGateway userGateway;
    private final CheckSubRolePermission permission;

    public MembershipOutputDto assignSubRoleToUser(UUID authenticatedUserId, MembershipInputDto inputDto) {
        boolean isAdmin = permission.checkAssignSubRole(authenticatedUserId, inputDto.restaurantId());
        var user = userGateway.getUserById(inputDto.userId())
                .orElseThrow(() -> new UserNotFoundException(inputDto.userId()));

        var restaurant = restaurantGateway.getRestaurantById(inputDto.restaurantId())
                .orElseThrow(() -> new RestaurantNotFoundException(inputDto.restaurantId()));

        var subRole = subRoleGateway.getById(inputDto.subRoleid())
                .orElseThrow(() -> new SubRoleNotFoundException(inputDto.subRoleid()));

        var foundMembership = membershipGateway.getUserRestaurantMembership(
                inputDto.userId(), inputDto.restaurantId());

        if (!isAdmin) {
            if (subRole.isMainOwner() || "ADMIN".equals(subRole.getRole().getValue())) {
                throw new ProtectedSubRoleException(subRole.getId());
            }
            foundMembership.ifPresent(existing -> {
                if (existing.getSubrole().isMainOwner()
                        || "ADMIN".equals(existing.getSubrole().getRole().getValue())) {
                    throw new ProtectedSubRoleException(existing.getSubrole().getId());
                }
            });
        }

        var id = foundMembership
                .map(Membership::getId)
                .orElse(null);

        var membership = new Membership(id, user, subRole, restaurant);
        var savedMembership = membershipGateway.save(membership);

        return new MembershipOutputDto(
                savedMembership.getId(),
                savedMembership.getUser().getId(),
                savedMembership.getSubrole().getId(),
                savedMembership.getRestaurant().getId()
        );
    }
}
