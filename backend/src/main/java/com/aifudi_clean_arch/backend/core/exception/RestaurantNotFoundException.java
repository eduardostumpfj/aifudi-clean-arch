package com.aifudi_clean_arch.backend.core.exception;

import java.util.UUID;

public class RestaurantNotFoundException extends RuntimeException {
    public RestaurantNotFoundException(UUID id) {
        super("Restaurant with id " + id + " not found");
    }
}
