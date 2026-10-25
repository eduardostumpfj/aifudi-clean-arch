package com.aifudi_clean_arch.backend.infra.database.mapper;

import lombok.RequiredArgsConstructor;
import com.aifudi_clean_arch.backend.core.domain.Membership;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.MembershipEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipMapper {
    private final UserMapper userMapper;
    private final SubRoleMapper subRoleMapper;
    private final RestaurantMapper restaurantMapper;


    public Membership map(MembershipEntity entity) {
        return new  Membership(
            entity.getId(),
            userMapper.map(entity.getUser()),
            subRoleMapper.map(entity.getSubrole()),
            restaurantMapper.map(entity.getRestaurant())
        );
    }

    public MembershipEntity map(Membership membership) {
        return new  MembershipEntity(
                membership.getId(),
                userMapper.map(membership.getUser()),
                subRoleMapper.map(membership.getSubrole()),
                restaurantMapper.map(membership.getRestaurant())
        );
    }
}
