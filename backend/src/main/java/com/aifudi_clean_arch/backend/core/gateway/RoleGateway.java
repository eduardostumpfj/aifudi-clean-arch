package com.aifudi_clean_arch.backend.core.gateway;

import com.aifudi_clean_arch.backend.core.domain.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleGateway {
    Optional<Role> getRole(UUID id);
}
