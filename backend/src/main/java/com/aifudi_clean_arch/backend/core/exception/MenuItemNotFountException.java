package com.aifudi_clean_arch.backend.core.exception;

import java.util.UUID;

public class MenuItemNotFountException extends RuntimeException {
    public MenuItemNotFountException(UUID id) {
        super("Menu item with id " + id + " is not fount");
    }
}
