package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.Membership;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.core.gateway.MembershipGateway;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.MembershipRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.MembershipMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MembershipJpaGateway implements MembershipGateway {
    private final MembershipRepository repository;
    private final MembershipMapper mapper;

    @Override
    public Optional<Membership> getUserRestaurantMembership(UUID userId, UUID restaurantId) {
        try {
            return repository.findByUser_IdAndRestaurant_Id(userId, restaurantId).map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving Membership", ex);
        }
    }

    @Override
    @Transactional
    public Membership save(Membership membership) {
        try {
            var saved = repository.save(mapper.map(membership));
            return mapper.map(saved);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error saving Membership", ex);
        }
    }

    @Override
    public Optional<Membership> getUserRestaurantMembership(Membership membership) {
        try {
            return repository.findByUser_IdAndRestaurant_Id(
                    membership.getUser().getId(),
                    membership.getRestaurant().getId())
                    .map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving Membership", ex);
        }
    }
}
