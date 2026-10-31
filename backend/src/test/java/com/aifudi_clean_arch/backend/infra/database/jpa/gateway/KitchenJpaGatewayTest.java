package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.*;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.*;
import com.aifudi_clean_arch.backend.infra.database.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KitchenJpaGatewayTest {
    private final KitchenRepository repository = mock(KitchenRepository.class, withSettings().mockMaker("mock-maker-subclass"));
    private final KitchenJpaGateway gateway = new KitchenJpaGateway(repository, new KitchenMapper());
    private final UUID id = UUID.randomUUID();

    @Test
    void mapsFoundRecord() {
        when(repository.findById(id)).thenReturn(Optional.of(new KitchenEntity(id, "BRAZILIAN", "Brasileira")));
        var result = gateway.getKitchenById(id).orElseThrow();
        assertAll(() -> assertEquals(id, result.getId()),
                () -> assertEquals("BRAZILIAN", result.getValue()),
                () -> assertEquals("Brasileira", result.getLabel()));
        verify(repository).findById(id);
    }

    @Test
    void returnsEmptyWhenRecordIsMissing() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertTrue(gateway.getKitchenById(id).isEmpty());
    }

    @Test
    void wrapsPersistenceFailureAndPreservesCause() {
        var failure = new DataAccessResourceFailureException("offline");
        when(repository.findById(id)).thenThrow(failure);
        var error = assertThrows(ErrorOnRepositoryAccess.class, () -> gateway.getKitchenById(id));
        assertSame(failure, error.getCause());
        assertTrue(error.getMessage().contains(id.toString()));
    }
}
