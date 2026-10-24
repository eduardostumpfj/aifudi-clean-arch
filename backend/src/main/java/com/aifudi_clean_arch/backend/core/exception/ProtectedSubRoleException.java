package com.aifudi_clean_arch.backend.core.exception;

import java.util.UUID;

public class ProtectedSubRoleException extends RuntimeException {
    public ProtectedSubRoleException(UUID id) {
        super("Invalid operation on Sub Role with id: " + id);
    }
    public ProtectedSubRoleException(String value) {
        super("Invalid operation on Sub Role with value: " + value);
    }
}
