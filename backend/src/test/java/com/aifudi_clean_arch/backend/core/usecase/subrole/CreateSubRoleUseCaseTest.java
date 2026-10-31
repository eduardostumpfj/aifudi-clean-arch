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

class CreateSubRoleUseCaseTest {
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
    private final CreateSubRoleUseCase useCase = new CreateSubRoleUseCase(roles, subroles, permission);

    @Test
    void createsWithoutIdAndReturnsPersistedSubrole() {
        when(roles.getRole(role.getId())).thenReturn(Optional.of(role));
        var saved = new SubRole(subroleId, "staff", "Equipe persistida", role);
        when(subroles.save(any())).thenReturn(saved);
        var result = useCase.createSubRole(userId, new CreateSubRoleInputDto("staff", "Equipe", role.getId()));
        assertEquals(new SubRoleOutputDto(subroleId, "staff", "Equipe persistida", role.getId()), result);
        var captor = ArgumentCaptor.forClass(SubRole.class);
        var order = inOrder(permission, roles, subroles);
        order.verify(permission).checkCreateSubRole(userId);
        order.verify(roles).getRole(role.getId());
        order.verify(subroles).save(captor.capture());
        var pending = captor.getValue();
        assertAll(() -> assertNull(pending.getId()),
                () -> assertEquals("staff", pending.getValue()),
                () -> assertEquals("Equipe", pending.getLabel()),
                () -> assertSame(role, pending.getRole()));
    }

    @Test
    void rejectsMissingRole() {
        when(roles.getRole(role.getId())).thenReturn(Optional.empty());
        assertThrows(RoleNotFoundException.class, () -> useCase.createSubRole(userId, new CreateSubRoleInputDto("staff", "Equipe", role.getId())));
        verifyNoInteractions(subroles);
    }

    @Test
    void rejectsReservedMainValue() {
        when(roles.getRole(role.getId())).thenReturn(Optional.of(role));
        assertThrows(ProtectedSubRoleException.class, () -> useCase.createSubRole(userId, new CreateSubRoleInputDto("main", "Principal", role.getId())));
        verifyNoInteractions(subroles);
    }

    @Test
    void rejectsInvalidFieldsBeforeSaving() {
        when(roles.getRole(role.getId())).thenReturn(Optional.of(role));
        for (var input : List.of(new CreateSubRoleInputDto(null, "Equipe", role.getId()),
                new CreateSubRoleInputDto(" ", "Equipe", role.getId()),
                new CreateSubRoleInputDto("staff", null, role.getId()),
                new CreateSubRoleInputDto("staff", " ", role.getId()))) {
            assertThrows(IllegalArgumentException.class, () -> useCase.createSubRole(userId, input));
        }
        verifyNoInteractions(subroles);
    }

    @Test
    void rejectsUnauthorizedUserBeforeAccessingGateways() {
        doThrow(new NotAllowedException()).when(permission).checkCreateSubRole(userId);
        assertThrows(NotAllowedException.class, () -> useCase.createSubRole(userId, new CreateSubRoleInputDto("staff", "Equipe", role.getId())));
        verifyNoInteractions(roles, subroles);
    }
}
