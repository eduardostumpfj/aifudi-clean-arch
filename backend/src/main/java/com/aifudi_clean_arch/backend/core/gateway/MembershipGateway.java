package com.aifudi_clean_arch.backend.core.gateway;

import com.aifudi_clean_arch.backend.core.domain.Membership;

import java.util.Optional;
import java.util.UUID;

public interface MembershipGateway {
    Optional<Membership> getUserRestaurantMembership(UUID userId, UUID restaurantId);
    Membership save(Membership membership);

    Optional<Membership> getUserRestaurantMembership(Membership membership);
}
