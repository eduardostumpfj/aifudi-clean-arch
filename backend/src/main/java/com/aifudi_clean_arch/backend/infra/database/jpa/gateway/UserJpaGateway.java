package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.User;
import com.aifudi_clean_arch.backend.core.gateway.UserGateway;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.UserRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserJpaGateway implements UserGateway {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public Optional<User> getUserById(UUID id) {
        try {
           return repository.findById(id).map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving User: " + id, ex);
        }
    }

}
