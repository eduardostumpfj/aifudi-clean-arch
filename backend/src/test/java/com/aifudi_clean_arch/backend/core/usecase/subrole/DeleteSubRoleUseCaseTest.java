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

class DeleteSubRoleUseCaseTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final SubRoleGateway subroles = mockType(SubRoleGateway.class);
    private final CheckSubRolePermission permission = mockType(CheckSubRolePermission.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID subroleId = UUID.randomUUID();
    private final Role role = new Role(UUID.randomUUID(), "OWNER", "Proprietário");
    private final SubRole current = new SubRole(subroleId, "staff", "Equipe", role);
    private final DeleteSubRoleUseCase useCase = new DeleteSubRoleUseCase(subroles, permission);

    @Test
    void deletesSubroleAfterCheckingPermissionAndProtection() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        when(subroles.delete(subroleId)).thenReturn(true);
        assertDoesNotThrow(() -> useCase.deleteSubRole(userId, subroleId));
        var order = inOrder(permission, subroles);
        order.verify(permission).checkDeleteSubRole(userId);
        order.verify(subroles).getById(subroleId);
        order.verify(subroles).delete(subroleId);
    }

    @Test
    void rejectsMissingSubroleBeforeDeletion() {
        when(subroles.getById(subroleId)).thenReturn(Optional.empty());
        assertThrows(SubRoleNotFoundException.class, () -> useCase.deleteSubRole(userId, subroleId));
        verify(subroles, never()).delete(any());
    }

    @Test
    void rejectsDeletingMainOwner() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(new SubRole(subroleId, "main", "Principal", role)));
        assertThrows(ProtectedSubRoleException.class, () -> useCase.deleteSubRole(userId, subroleId));
        verify(subroles, never()).delete(any());
    }

    @Test
    void throwsWhenDeletionReportsMissingSubrole() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        when(subroles.delete(subroleId)).thenReturn(false);
        assertThrows(SubRoleNotFoundException.class, () -> useCase.deleteSubRole(userId, subroleId));
        verify(subroles).delete(subroleId);
    }

    @Test
    void rejectsUnauthorizedUserWithoutAccessingGateway() {
        doThrow(new NotAllowedException()).when(permission).checkDeleteSubRole(userId);
        assertThrows(NotAllowedException.class, () -> useCase.deleteSubRole(userId, subroleId));
        verifyNoInteractions(subroles);
    }
}
