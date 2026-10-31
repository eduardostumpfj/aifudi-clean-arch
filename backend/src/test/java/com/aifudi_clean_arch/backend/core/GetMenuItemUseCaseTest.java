package com.aifudi_clean_arch.backend.core;

import com.aifudi_clean_arch.backend.core.usecase.menuItem.GetMenuItemUseCase;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.CheckMenuItemPermission;

import com.aifudi_clean_arch.backend.core.domain.MenuItem;
import com.aifudi_clean_arch.backend.core.dto.menuItem.MenuItemOutputDto;
import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.core.exception.NotAllowedException;
import com.aifudi_clean_arch.backend.core.gateway.MenuItemGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetMenuItemUseCaseTest {
    private final MenuItemGateway gateway = mock(MenuItemGateway.class,
            withSettings().mockMaker("mock-maker-subclass"));
    private final CheckMenuItemPermission permission = mock(CheckMenuItemPermission.class,
            withSettings().mockMaker("mock-maker-subclass"));
    private final GetMenuItemUseCase useCase = new GetMenuItemUseCase(gateway, permission);
    private final UUID userId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();

    @Test
    @DisplayName("Retorna todos os dados do item após verificar a permissão")
    void returnsItemWhenFoundAndUserIsAllowed() {
        var restaurantId = UUID.randomUUID();
        var price = new BigDecimal("29.90");
        var item = new MenuItem(itemId, "Hambúrguer", "Pão, carne e queijo", price,
                true, "burger.jpg", restaurantId);
        when(gateway.getById(itemId)).thenReturn(Optional.of(item));

        var result = useCase.getMenuItemById(userId, itemId);

        assertEquals(new MenuItemOutputDto(itemId, "Hambúrguer", "Pão, carne e queijo",
                price, true, "burger.jpg", restaurantId), result);
        var order = inOrder(permission, gateway);
        order.verify(permission).checkGetMenuItem(userId);
        order.verify(gateway).getById(itemId);
    }

    @Test
    @DisplayName("Lança exceção quando o item não existe")
    void throwsWhenItemIsNotFound() {
        when(gateway.getById(itemId)).thenReturn(Optional.empty());

        assertThrows(MenuItemNotFountException.class,
                () -> useCase.getMenuItemById(userId, itemId));

        verify(permission).checkGetMenuItem(userId);
        verify(gateway).getById(itemId);
    }

    @Test
    @DisplayName("Não consulta o item quando o usuário não tem permissão")
    void doesNotAccessGatewayWhenPermissionIsDenied() {
        var denied = new NotAllowedException();
        doThrow(denied).when(permission).checkGetMenuItem(userId);

        var error = assertThrows(NotAllowedException.class,
                () -> useCase.getMenuItemById(userId, itemId));

        assertSame(denied, error);
        verifyNoInteractions(gateway);
    }
}
