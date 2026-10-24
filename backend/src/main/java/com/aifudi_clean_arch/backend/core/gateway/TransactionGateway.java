package com.aifudi_clean_arch.backend.core.gateway;

import java.util.function.Supplier;

public interface TransactionGateway {
    <T> T execute(Supplier<T> supplier);
}
