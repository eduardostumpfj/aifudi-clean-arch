package com.aifudi_clean_arch.backend.core.usecase.subrole;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.subrole.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateSubRoleUseCaseTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final SubRoleGateway subroles = mockType(SubRoleGateway.class);
    private final CheckSubRolePermission permission = mockType(CheckSubRolePermission.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID subroleId = UUID.randomUUID();
    private final Role role = new Role(UUID.randomUUID(), "OWNER", "Proprietário");
    private final SubRole current = new SubRole(subroleId, "staff", "Equipe", role);
    private final RoleGateway roles = mockType(RoleGateway.class);
    private final UpdateSubRoleUseCase useCase = new UpdateSubRoleUseCase(roles, subroles, permission);

    private UpdateSubRoleInputDto emptyInput() {
        return new UpdateSubRoleInputDto(subroleId, Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Test
    void updatesAllFieldsAndRole() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        var newRole = new Role(UUID.randomUUID(), "USER", "Usuário");
        when(roles.getRole(newRole.getId())).thenReturn(Optional.of(newRole));
        when(subroles.update(any())).thenAnswer(i -> i.getArgument(0));
        var input = new UpdateSubRoleInputDto(subroleId, Optional.of("customer"), Optional.of("Cliente"), Optional.of(newRole.getId()));
        var result = useCase.update(userId, input);
        assertEquals(new SubRoleOutputDto(subroleId, "customer", "Cliente", newRole.getId()), result);
        var captor = ArgumentCaptor.forClass(SubRole.class);
        var order = inOrder(permission, subroles, roles);
        order.verify(permission).checkUpdateSubRole(userId);
        order.verify(subroles).getById(subroleId);
        order.verify(roles).getRole(newRole.getId());
        order.verify(subroles).update(captor.capture());
        var updated = captor.getValue();
        assertAll(() -> assertEquals(subroleId, updated.getId()),
                () -> assertEquals("customer", updated.getValue()),
                () -> assertEquals("Cliente", updated.getLabel()),
                () -> assertSame(newRole, updated.getRole()));
    }

    @Test
    void preservesOmittedFieldsAndReturnsPersistedData() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        var saved = new SubRole(subroleId, "staff", "Equipe persistida", role);
        when(subroles.update(any())).thenReturn(saved);
        assertEquals(new SubRoleOutputDto(subroleId, "staff", "Equipe persistida", role.getId()), useCase.update(userId, emptyInput()));
        var captor = ArgumentCaptor.forClass(SubRole.class);
        verify(subroles).update(captor.capture());
        var unchanged = captor.getValue();
        assertAll(() -> assertEquals(current.getId(), unchanged.getId()),
                () -> assertEquals(current.getValue(), unchanged.getValue()),
                () -> assertEquals(current.getLabel(), unchanged.getLabel()),
                () -> assertSame(role, unchanged.getRole()));
        verifyNoInteractions(roles);
    }

    @Test
    void rejectsMissingSubrole() {
        when(subroles.getById(subroleId)).thenReturn(Optional.empty());
        assertThrows(SubRoleNotFoundException.class, () -> useCase.update(userId, emptyInput()));
        verify(subroles, never()).update(any());
        verifyNoInteractions(roles);
    }

    @Test
    void rejectsChangingMainOwner() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(new SubRole(subroleId, "main", "Principal", role)));
        assertThrows(ProtectedSubRoleException.class, () -> useCase.update(userId, emptyInput()));
        verify(subroles, never()).update(any());
        verifyNoInteractions(roles);
    }

    @Test
    void rejectsMissingNewRole() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        var missingId = UUID.randomUUID();
        when(roles.getRole(missingId)).thenReturn(Optional.empty());
        var input = new UpdateSubRoleInputDto(subroleId, Optional.empty(), Optional.empty(), Optional.of(missingId));
        assertThrows(RoleNotFoundException.class, () -> useCase.update(userId, input));
        verify(subroles, never()).update(any());
    }

    @Test
    void rejectsRenamingToReservedMain() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        var input = new UpdateSubRoleInputDto(subroleId, Optional.of("main"), Optional.empty(), Optional.empty());
        assertThrows(ProtectedSubRoleException.class, () -> useCase.update(userId, input));
        verify(subroles, never()).update(any());
    }

    @Test
    void rejectsBlankLabel() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        var input = new UpdateSubRoleInputDto(subroleId, Optional.empty(), Optional.of(" "), Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.update(userId, input));
        verify(subroles, never()).update(any());
    }

    @Test
    void rejectsUnauthorizedUserBeforeAccessingGateways() {
        doThrow(new NotAllowedException()).when(permission).checkUpdateSubRole(userId);
        assertThrows(NotAllowedException.class, () -> useCase.update(userId, emptyInput()));
        verifyNoInteractions(subroles, roles);
    }
}
