package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.SubRole;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.core.exception.SubRoleNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.SubRoleRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.SubRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubRoleJpaGateway implements SubRoleGateway {
    private final SubRoleRepository repository;
    private final SubRoleMapper mapper;

    @Override
    @Transactional
    public SubRole save(SubRole subRole) {
        try {
            var saved = repository.save(mapper.map(subRole));
            return mapper.map(saved);

        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error saving subrole", ex);
        }
    }

    @Override
    public Optional<SubRole> getById(UUID id) {
        try {
            return repository.findById(id).map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving subrole: " + id, ex);
        }
    }

    @Override
    public Optional<SubRole> getByValue(String value) {
        try {
            return repository.findByValue(value).map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving subrole: " + value, ex);
        }
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        try {
            return repository.deleteByIdReturningCount(id) > 0;
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error deleting subrole: " + id, ex);
        }
    }

    @Override
    @Transactional
    public SubRole update(SubRole subRole) {
        try {
            var current =  repository.findById(subRole.getId())
                    .orElseThrow(() -> new SubRoleNotFoundException(subRole.getId()));

            current.setValue(subRole.getValue());
            current.setLabel(subRole.getLabel());
            current.setRole(mapper.map(subRole).getRole());

            return mapper.map(repository.save(current));
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error updating subrole: " + subRole.getId(), ex);
        }
    }
}
