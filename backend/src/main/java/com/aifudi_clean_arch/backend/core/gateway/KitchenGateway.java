package com.aifudi_clean_arch.backend.core.gateway;

import com.aifudi_clean_arch.backend.core.domain.Kitchen;

import java.util.Optional;
import java.util.UUID;

public interface KitchenGateway {
    Optional<Kitchen> getKitchenById(UUID id);
}
