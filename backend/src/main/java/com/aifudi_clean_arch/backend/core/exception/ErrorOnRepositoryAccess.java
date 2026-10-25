package com.aifudi_clean_arch.backend.core.exception;

public class ErrorOnRepositoryAccess extends RuntimeException {
    public ErrorOnRepositoryAccess(String message, Throwable cause) {
        super(message, cause);
    }
}
