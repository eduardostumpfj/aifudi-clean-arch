package com.aifudi_clean_arch.backend.core.exception;

import java.util.UUID;

public class KitchenNotFoundException extends RuntimeException {
    public KitchenNotFoundException(UUID id) {
        super("Kitchen with id " + id + " not found");
    }
}
