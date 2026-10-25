package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionJpaGatewayTest {
    private final PlatformTransactionManager manager = mock(PlatformTransactionManager.class,
            withSettings().mockMaker("mock-maker-subclass"));
    private final SimpleTransactionStatus status = new SimpleTransactionStatus();
    private final TransactionJpaGateway gateway = new TransactionJpaGateway(manager);

    @Test
    void commitsAndReturnsOperationResult() {
        when(manager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);

        assertEquals("result", gateway.execute(() -> "result"));

        verify(manager).commit(status);
        verify(manager, never()).rollback(any());
    }

    @Test
    void rollsBackAndPreservesOperationFailure() {
        when(manager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        var failure = new IllegalStateException("Failed to save membership");

        var thrown = assertThrows(IllegalStateException.class,
                () -> gateway.execute(() -> { throw failure; }));

        assertSame(failure, thrown);
        verify(manager).rollback(status);
        verify(manager, never()).commit(any());
    }
}
