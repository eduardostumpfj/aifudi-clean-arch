package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.Kitchen;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.core.gateway.KitchenGateway;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.KitchenRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.KitchenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class KitchenJpaGateway implements KitchenGateway {
    private final KitchenRepository repository;
    private final KitchenMapper mapper;

    @Override
    public Optional<Kitchen> getKitchenById(UUID id) {
        try {
            return repository.findById(id).map(mapper::map);
        } catch (DataAccessException ex) {
            throw new ErrorOnRepositoryAccess("Error retrieving Kitchen: " + id, ex);
        }
    }
}
