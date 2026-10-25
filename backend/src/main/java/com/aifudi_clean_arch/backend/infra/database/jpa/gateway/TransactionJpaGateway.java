package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.gateway.TransactionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TransactionJpaGateway implements TransactionGateway {
    private final TransactionTemplate transactionTemplate;

    @Override
    public <T> T execute(Supplier<T> supplier) {
        return transactionTemplate.execute(status -> supplier.get());
    }
}
