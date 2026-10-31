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

class MenuItemControllerTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final CreateMenuItemUseCase create = mockType(CreateMenuItemUseCase.class);
    private final DeleteMenuItemUseCase delete = mockType(DeleteMenuItemUseCase.class);
    private final UpdateMenuItemUseCase update = mockType(UpdateMenuItemUseCase.class);
    private final GetMenuItemUseCase get = mockType(GetMenuItemUseCase.class);
    private final MenuItemController controller = new MenuItemController(create, delete, update, get);
    private final UUID userId = UUID.randomUUID();
    private final UUID id = UUID.randomUUID();
    private final UUID referenceId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @Test
    void delegatesCreateWithAuthenticatedUserAndOriginalInput() {
        var input = new CreateMenuItemInputDto("Item", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId);
        var output = new MenuItemOutputDto(id, "Item", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId);
        when(create.createMenuItem(userId, input)).thenReturn(output);

        var result = controller.createMenuItem(userId, input);

        assertSame(output, result);
        verify(create).createMenuItem(userId, input);
        verifyNoMoreInteractions(create, delete, update, get);
    }

    @Test
    void propagatesCreateFailureWithoutCallingOtherUseCases() {
        var input = new CreateMenuItemInputDto("Item", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId);
        var failure = new NotAllowedException();
        when(create.createMenuItem(userId, input)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.createMenuItem(userId, input));

        assertSame(failure, error);
        verify(create).createMenuItem(userId, input);
        verifyNoMoreInteractions(create, delete, update, get);
    }

    @Test
    void delegatesDeleteWithAuthenticatedUserAndOriginalInput() {
        assertDoesNotThrow(() -> controller.deleteMenuItem(userId, id));
        verify(delete).deleteMenuItem(userId, id);
        verifyNoMoreInteractions(create, delete, update, get);
    }

    @Test
    void propagatesDeleteFailureWithoutCallingOtherUseCases() {
        var failure = new NotAllowedException();
        doThrow(failure).when(delete).deleteMenuItem(userId, id);

        var error = assertThrows(NotAllowedException.class, () -> controller.deleteMenuItem(userId, id));

        assertSame(failure, error);
        verify(delete).deleteMenuItem(userId, id);
        verifyNoMoreInteractions(create, delete, update, get);
    }

    @Test
    void delegatesUpdateWithAuthenticatedUserAndOriginalInput() {
        var input = new UpdateMenuItemInputDto(id, Optional.of("Atualizado"), Optional.empty(), Optional.empty(), Optional.of(false), Optional.empty(), Optional.empty());
        var output = new MenuItemOutputDto(id, "Atualizado", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId);
        when(update.updateMenuItem(userId, input)).thenReturn(output);

        var result = controller.updateMenuItem(userId, input);

        assertSame(output, result);
        verify(update).updateMenuItem(userId, input);
        verifyNoMoreInteractions(create, delete, update, get);
    }

    @Test
    void propagatesUpdateFailureWithoutCallingOtherUseCases() {
        var input = new UpdateMenuItemInputDto(id, Optional.of("Atualizado"), Optional.empty(), Optional.empty(), Optional.of(false), Optional.empty(), Optional.empty());
        var failure = new NotAllowedException();
        when(update.updateMenuItem(userId, input)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.updateMenuItem(userId, input));

        assertSame(failure, error);
        verify(update).updateMenuItem(userId, input);
        verifyNoMoreInteractions(create, delete, update, get);
    }

    @Test
    void delegatesGetWithAuthenticatedUserAndOriginalInput() {
        var output = new MenuItemOutputDto(id, "Item", "Descrição", BigDecimal.TEN, true, "item.jpg", referenceId);
        when(get.getMenuItemById(userId, id)).thenReturn(output);

        var result = controller.getMenuItem(userId, id);

        assertSame(output, result);
        verify(get).getMenuItemById(userId, id);
        verifyNoMoreInteractions(create, delete, update, get);
    }

    @Test
    void propagatesGetFailureWithoutCallingOtherUseCases() {
        var failure = new NotAllowedException();
        when(get.getMenuItemById(userId, id)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.getMenuItem(userId, id));

        assertSame(failure, error);
        verify(get).getMenuItemById(userId, id);
        verifyNoMoreInteractions(create, delete, update, get);
    }
}
