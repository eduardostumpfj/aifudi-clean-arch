package com.aifudi_clean_arch.backend.infra.database.mapper;

import com.aifudi_clean_arch.backend.core.domain.SubRole;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.SubRoleEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubRoleMapper {
    private final RoleMapper roleMapper;

    public SubRoleEntity map(SubRole subRole) {
        return new SubRoleEntity(
                subRole.getId(),
                roleMapper.map(subRole.getRole()),
                subRole.getValue(),
                subRole.getLabel()
        );
    }

    public SubRole map(SubRoleEntity entity) {
        return new SubRole(
                entity.getId(),
                entity.getValue(),
                entity.getLabel(),
                roleMapper.map(entity.getRole())
        );
    }
}
