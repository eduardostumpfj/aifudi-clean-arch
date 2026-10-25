package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.exception.SubRoleNotFoundException;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.*;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.SubRoleRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubRoleJpaGatewayTest {
    private final SubRoleRepository repository = mock(SubRoleRepository.class, withSettings().mockMaker("mock-maker-subclass"));
    private final SubRoleJpaGateway gateway = new SubRoleJpaGateway(repository, new SubRoleMapper(new RoleMapper()));
    private final UUID id = UUID.randomUUID();

    @Test
    void updatesRoleAsWellAsLabelAndValue() {
        var current = new SubRoleEntity(id, new RoleEntity(UUID.randomUUID(), "OLD", "Old"), "old", "Old");
        var role = new Role(UUID.randomUUID(), "NEW", "New");
        when(repository.findById(id)).thenReturn(Optional.of(current));
        when(repository.save(current)).thenReturn(current);
        var saved = gateway.update(new SubRole(id, "new", "New", role));
        assertEquals(role.getId(), saved.getRole().getId());
        assertEquals("new", saved.getValue());
        assertEquals("New", saved.getLabel());
        verify(repository).save(current);
    }

    @Test
    void missingUpdateDoesNotInsert() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(SubRoleNotFoundException.class, () -> gateway.update(new SubRole(id, "v", "Label", new Role(UUID.randomUUID(), "R", "Role"))));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteReportsWhetherRecordExisted() {
        when(repository.deleteByIdReturningCount(id)).thenReturn(0, 1);
        assertFalse(gateway.delete(id));
        assertTrue(gateway.delete(id));
    }
}
