package com.aifudi_clean_arch.backend.core.usecase.menuItem;

import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteMenuItemUseCaseTest {
    private final MenuItemGateway items = mock(MenuItemGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final CheckMenuItemPermission permission = mock(CheckMenuItemPermission.class, withSettings().mockMaker("mock-maker-subclass"));
    private final DeleteMenuItemUseCase useCase = new DeleteMenuItemUseCase(items, permission);
    private final UUID userId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();

    @Test
    void deletesItemAfterCheckingPermission() {
        when(items.delete(itemId)).thenReturn(true);
        assertDoesNotThrow(() -> useCase.deleteMenuItem(userId, itemId));
        var order = inOrder(permission, items);
        order.verify(permission).checkDeleteMenuItem(userId, itemId);
        order.verify(items).delete(itemId);
    }

    @Test
    void throwsWhenItemWasNotDeleted() {
        when(items.delete(itemId)).thenReturn(false);
        assertThrows(MenuItemNotFountException.class, () -> useCase.deleteMenuItem(userId, itemId));
        verify(items).delete(itemId);
    }

    @Test
    void doesNotDeleteWhenPermissionIsDenied() {
        doThrow(new NotAllowedException()).when(permission).checkDeleteMenuItem(userId, itemId);
        assertThrows(NotAllowedException.class, () -> useCase.deleteMenuItem(userId, itemId));
        verifyNoInteractions(items);
    }
}
