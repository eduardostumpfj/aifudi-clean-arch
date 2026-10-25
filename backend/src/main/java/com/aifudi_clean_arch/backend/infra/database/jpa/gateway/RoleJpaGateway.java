package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.Role;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.core.gateway.RoleGateway;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.RoleEntity;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.RoleRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoleJpaGateway implements RoleGateway {
    private final RoleRepository repository;
    private final RoleMapper mapper;

    @Override
    public Optional<Role> getRole(UUID id) {
        try {
            return repository.findById(id).map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving role: " + id, ex);
        }
    }
}
