package com.aifudi_clean_arch.backend.infra.database.mapper;

import com.aifudi_clean_arch.backend.core.domain.Role;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.RoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    public Role map(RoleEntity entity) {
        return new Role(entity.getId(), entity.getValue(), entity.getLabel());
    }

    public RoleEntity map(Role role) {
        return new RoleEntity(role.getId(), role.getValue(), role.getLabel());
    }
}
