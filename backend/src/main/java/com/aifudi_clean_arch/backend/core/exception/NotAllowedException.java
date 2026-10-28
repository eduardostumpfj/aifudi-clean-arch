package com.aifudi_clean_arch.backend.core.exception;

public class NotAllowedException extends RuntimeException {
    public NotAllowedException() {
        super("Not allowed to access this resource");
    }
}
