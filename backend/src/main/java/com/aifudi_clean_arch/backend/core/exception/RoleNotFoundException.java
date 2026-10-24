package com.aifudi_clean_arch.backend.core.exception;

import java.util.UUID;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(UUID id) {
        super("Role with id " + id + " not found");
    }
}