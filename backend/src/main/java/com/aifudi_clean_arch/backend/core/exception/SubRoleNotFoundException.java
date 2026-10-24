package com.aifudi_clean_arch.backend.core.exception;

import java.util.UUID;

public class SubRoleNotFoundException extends RuntimeException {
    public SubRoleNotFoundException(UUID id) {
        super("SubRole with id " + id + " not found");
    }

    public SubRoleNotFoundException(String value) {
        super("SubRole with value " + value + " not found");
    }
}
