package com.aifudi_clean_arch.backend.core.gateway;

import com.aifudi_clean_arch.backend.core.domain.MenuItem;

import java.util.Optional;
import java.util.UUID;

public interface MenuItemGateway {
    MenuItem save(MenuItem menuItem);
    boolean delete(UUID id);
    MenuItem update(MenuItem menuItem);
    Optional<MenuItem> getById(UUID id);
}
