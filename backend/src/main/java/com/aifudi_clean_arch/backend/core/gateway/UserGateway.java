package com.aifudi_clean_arch.backend.core.gateway;

import com.aifudi_clean_arch.backend.core.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserGateway {
    Optional<User> getUserById(UUID userId);
    Optional<User> getUserByLogin(String login);
}
