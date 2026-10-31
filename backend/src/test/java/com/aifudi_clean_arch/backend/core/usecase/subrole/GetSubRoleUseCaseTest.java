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

class GetSubRoleUseCaseTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final SubRoleGateway subroles = mockType(SubRoleGateway.class);
    private final CheckSubRolePermission permission = mockType(CheckSubRolePermission.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID subroleId = UUID.randomUUID();
    private final Role role = new Role(UUID.randomUUID(), "OWNER", "Proprietário");
    private final SubRole current = new SubRole(subroleId, "staff", "Equipe", role);
    private final GetSubRoleUseCase useCase = new GetSubRoleUseCase(subroles, permission);

    @Test
    void returnsAllSubroleDataAfterPermissionCheck() {
        when(subroles.getById(subroleId)).thenReturn(Optional.of(current));
        var result = useCase.getSubRoleById(userId, subroleId);
        assertEquals(new SubRoleOutputDto(subroleId, "staff", "Equipe", role.getId()), result);
        var order = inOrder(permission, subroles);
        order.verify(permission).checkGetSubRole(userId);
        order.verify(subroles).getById(subroleId);
    }

    @Test
    void throwsWhenSubroleIsMissing() {
        when(subroles.getById(subroleId)).thenReturn(Optional.empty());
        assertThrows(SubRoleNotFoundException.class, () -> useCase.getSubRoleById(userId, subroleId));
    }

    @Test
    void rejectsUnauthorizedUserWithoutQueryingSubrole() {
        doThrow(new NotAllowedException()).when(permission).checkGetSubRole(userId);
        assertThrows(NotAllowedException.class, () -> useCase.getSubRoleById(userId, subroleId));
        verifyNoInteractions(subroles);
    }
}
