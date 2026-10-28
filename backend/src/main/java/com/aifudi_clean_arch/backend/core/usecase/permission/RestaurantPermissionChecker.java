package com.aifudi_clean_arch.backend.core.usecase.permission;

import com.aifudi_clean_arch.backend.core.domain.User;
import com.aifudi_clean_arch.backend.core.exception.NotAllowedException;
import com.aifudi_clean_arch.backend.core.gateway.MembershipGateway;
import com.aifudi_clean_arch.backend.core.gateway.UserGateway;
import lombok.AllArgsConstructor;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class RestaurantPermissionChecker {
    private final UserGateway userGateway;
    private final MembershipGateway membershipGateway;

    private User getUser(UUID userId) {
        if (userId == null) {
            throw new NotAllowedException();
        }
        return userGateway.getUserById(userId).orElseThrow(NotAllowedException::new);
    }

    public void checkRoles(UUID userId, String... allowedRoles) {
        var role = getUser(userId).getClientSubrole().getRole().getValue();
        if (!Set.of(allowedRoles).contains(role)) {
            throw new NotAllowedException();
        }
    }

    public void checkManageRestaurant(UUID userId, UUID restaurantId) {
        var user = getUser(userId);
        if ("ADMIN".equals(user.getClientSubrole().getRole().getValue())) {
            return;
        }
        if (restaurantId == null) {
            throw new NotAllowedException();
        }
        var membership = membershipGateway.getUserRestaurantMembership(userId, restaurantId)
                .orElseThrow(NotAllowedException::new);
        var role = membership.getSubrole().getRole().getValue();
        if (!"OWNER".equals(role) && !"ADMIN".equals(role)) {
            throw new NotAllowedException();
        }
    }
}
