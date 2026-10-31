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

    @Test
    void savesNewSubroleAndReturnsGeneratedId() {
        var role = new Role(UUID.randomUUID(), "OWNER", "Dono");
        var generatedId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(i -> {
            SubRoleEntity entity = i.getArgument(0);
            assertNull(entity.getId());
            assertEquals(role.getId(), entity.getRole().getId());
            assertEquals("staff", entity.getValue());
            assertEquals("Equipe", entity.getLabel());
            entity.setId(generatedId);
            return entity;
        });
        var saved = gateway.save(new SubRole(null, "staff", "Equipe", role));
        assertEquals(generatedId, saved.getId());
        assertEquals(role.getId(), saved.getRole().getId());
    }

    @Test
    void looksUpByIdAndValueAndReturnsEmptyWhenMissing() {
        var entity = new SubRoleEntity(id, new RoleEntity(UUID.randomUUID(), "OWNER", "Dono"), "staff", "Equipe");
        when(repository.findById(id)).thenReturn(Optional.of(entity), Optional.empty());
        when(repository.findByValue("staff")).thenReturn(Optional.of(entity), Optional.empty());
        var byId = gateway.getById(id).orElseThrow();
        var byValue = gateway.getByValue("staff").orElseThrow();
        assertAll(() -> assertEquals(id, byId.getId()),
                () -> assertEquals("staff", byId.getValue()),
                () -> assertEquals("Equipe", byId.getLabel()),
                () -> assertEquals(entity.getRole().getId(), byId.getRole().getId()),
                () -> assertEquals(id, byValue.getId()),
                () -> assertEquals("staff", byValue.getValue()));
        assertTrue(gateway.getById(id).isEmpty());
        assertTrue(gateway.getByValue("staff").isEmpty());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"save", "id", "value", "delete", "update"})
    void wrapsRepositoryFailuresAndPreservesCause(String operation) {
        var failure = new org.springframework.dao.DataAccessResourceFailureException("offline");
        var subrole = new SubRole(id, "staff", "Equipe", new Role(UUID.randomUUID(), "OWNER", "Dono"));
        when(repository.save(any())).thenThrow(failure);
        when(repository.findById(id)).thenThrow(failure);
        when(repository.findByValue("staff")).thenThrow(failure);
        when(repository.deleteByIdReturningCount(id)).thenThrow(failure);
        var error = assertThrows(com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess.class, () -> {
            switch (operation) {
                case "save" -> gateway.save(subrole);
                case "id" -> gateway.getById(id);
                case "value" -> gateway.getByValue("staff");
                case "delete" -> gateway.delete(id);
                default -> gateway.update(subrole);
            }
        });
        assertSame(failure, error.getCause());
    }
}
