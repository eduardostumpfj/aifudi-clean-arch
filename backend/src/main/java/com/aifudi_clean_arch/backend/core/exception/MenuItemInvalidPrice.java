package com.aifudi_clean_arch.backend.core.exception;

import java.util.UUID;

public class MenuItemInvalidPrice extends RuntimeException {
    public MenuItemInvalidPrice(UUID id) {
        super("Invalid price on menu item: " + id);
    }
}
