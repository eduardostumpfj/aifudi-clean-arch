package com.aifudi_clean_arch.backend.core.controller;

import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.core.dto.menuItem.*;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.dto.membership.*;
import com.aifudi_clean_arch.backend.core.exception.NotAllowedException;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.*;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.*;
import com.aifudi_clean_arch.backend.core.usecase.subrole.*;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubRoleControllerTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final CreateSubRoleUseCase create = mockType(CreateSubRoleUseCase.class);
    private final UpdateSubRoleUseCase update = mockType(UpdateSubRoleUseCase.class);
    private final DeleteSubRoleUseCase delete = mockType(DeleteSubRoleUseCase.class);
    private final GetSubRoleUseCase get = mockType(GetSubRoleUseCase.class);
    private final AssignSubRoleToUserUseCase assign = mockType(AssignSubRoleToUserUseCase.class);
    private final SubRoleController controller = new SubRoleController(create, update, delete, get, assign);
    private final UUID userId = UUID.randomUUID();
    private final UUID id = UUID.randomUUID();
    private final UUID referenceId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @Test
    void delegatesCreateWithAuthenticatedUserAndOriginalInput() {
        var input = new CreateSubRoleInputDto("staff", "Equipe", referenceId);
        var output = new SubRoleOutputDto(id, "staff", "Equipe", referenceId);
        when(create.createSubRole(userId, input)).thenReturn(output);

        var result = controller.createSubRole(userId, input);

        assertSame(output, result);
        verify(create).createSubRole(userId, input);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void propagatesCreateFailureWithoutCallingOtherUseCases() {
        var input = new CreateSubRoleInputDto("staff", "Equipe", referenceId);
        var failure = new NotAllowedException();
        when(create.createSubRole(userId, input)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.createSubRole(userId, input));

        assertSame(failure, error);
        verify(create).createSubRole(userId, input);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void delegatesUpdateWithAuthenticatedUserAndOriginalInput() {
        var input = new UpdateSubRoleInputDto(id, Optional.empty(), Optional.of("Atualizado"), Optional.empty());
        var output = new SubRoleOutputDto(id, "staff", "Atualizado", referenceId);
        when(update.update(userId, input)).thenReturn(output);

        var result = controller.updateSubRole(userId, input);

        assertSame(output, result);
        verify(update).update(userId, input);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void propagatesUpdateFailureWithoutCallingOtherUseCases() {
        var input = new UpdateSubRoleInputDto(id, Optional.empty(), Optional.of("Atualizado"), Optional.empty());
        var failure = new NotAllowedException();
        when(update.update(userId, input)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.updateSubRole(userId, input));

        assertSame(failure, error);
        verify(update).update(userId, input);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void delegatesDeleteWithAuthenticatedUserAndOriginalInput() {
        assertDoesNotThrow(() -> controller.deleteSubRole(userId, id));
        verify(delete).deleteSubRole(userId, id);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void propagatesDeleteFailureWithoutCallingOtherUseCases() {
        var failure = new NotAllowedException();
        doThrow(failure).when(delete).deleteSubRole(userId, id);

        var error = assertThrows(NotAllowedException.class, () -> controller.deleteSubRole(userId, id));

        assertSame(failure, error);
        verify(delete).deleteSubRole(userId, id);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void delegatesGetWithAuthenticatedUserAndOriginalInput() {
        var output = new SubRoleOutputDto(id, "staff", "Equipe", referenceId);
        when(get.getSubRoleById(userId, id)).thenReturn(output);

        var result = controller.getSubRole(userId, id);

        assertSame(output, result);
        verify(get).getSubRoleById(userId, id);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void propagatesGetFailureWithoutCallingOtherUseCases() {
        var failure = new NotAllowedException();
        when(get.getSubRoleById(userId, id)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.getSubRole(userId, id));

        assertSame(failure, error);
        verify(get).getSubRoleById(userId, id);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void delegatesAssignWithAuthenticatedUserAndOriginalInput() {
        var input = new MembershipInputDto(ownerId, id, referenceId);
        var output = new MembershipOutputDto(UUID.randomUUID(), ownerId, id, referenceId);
        when(assign.assignSubRoleToUser(userId, input)).thenReturn(output);

        var result = controller.assignSubRoleToUserUseCase(userId, input);

        assertSame(output, result);
        verify(assign).assignSubRoleToUser(userId, input);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }

    @Test
    void propagatesAssignFailureWithoutCallingOtherUseCases() {
        var input = new MembershipInputDto(ownerId, id, referenceId);
        var failure = new NotAllowedException();
        when(assign.assignSubRoleToUser(userId, input)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.assignSubRoleToUserUseCase(userId, input));

        assertSame(failure, error);
        verify(assign).assignSubRoleToUser(userId, input);
        verifyNoMoreInteractions(create, update, delete, get, assign);
    }
}
