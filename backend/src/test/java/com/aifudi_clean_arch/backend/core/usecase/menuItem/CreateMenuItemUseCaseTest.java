package com.aifudi_clean_arch.backend.core.usecase.menuItem;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.dto.menuItem.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateMenuItemUseCaseTest {
    private final RestaurantGateway restaurants = mock(RestaurantGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MenuItemGateway items = mock(MenuItemGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final CheckMenuItemPermission permission = mock(CheckMenuItemPermission.class, withSettings().mockMaker("mock-maker-subclass"));
    private final CreateMenuItemUseCase useCase = new CreateMenuItemUseCase(restaurants, items, permission);
    private final UUID userId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();

    private CreateMenuItemInputDto input(BigDecimal price) {
        return new CreateMenuItemInputDto("Hambúrguer", "Pão, carne e queijo", price, true, "burger.jpg", restaurantId);
    }

    private void restaurantExists() {
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.of(new Restaurant(restaurantId,
                "Restaurante", "Endereço", "11:00 - 22:00", new Kitchen(UUID.randomUUID(), "BRAZILIAN", "Brasileira"), Set.of(), Set.of())));
    }

    @Test
    void createsItemWithoutIdAndReturnsPersistedData() {
        restaurantExists();
        var generatedId = UUID.randomUUID();
        var price = new BigDecimal("29.90");
        when(items.save(any())).thenReturn(new MenuItem(generatedId, "Hambúrguer", "Pão, carne e queijo", price,
                true, "burger.jpg", restaurantId));

        var result = useCase.createMenuItem(userId, input(price));

        assertEquals(new MenuItemOutputDto(generatedId, "Hambúrguer", "Pão, carne e queijo", price,
                true, "burger.jpg", restaurantId), result);
        var captor = ArgumentCaptor.forClass(MenuItem.class);
        var order = inOrder(permission, restaurants, items);
        order.verify(permission).checkCreateMenuItem(userId, restaurantId);
        order.verify(restaurants).getRestaurantById(restaurantId);
        order.verify(items).save(captor.capture());
        var pending = captor.getValue();
        assertAll(() -> assertNull(pending.getId()),
                () -> assertEquals("Hambúrguer", pending.getName()),
                () -> assertEquals("Pão, carne e queijo", pending.getDescription()),
                () -> assertEquals(price, pending.getPrice()),
                () -> assertTrue(pending.isHasDelivery()),
                () -> assertEquals("burger.jpg", pending.getPicture()),
                () -> assertEquals(restaurantId, pending.getRestaurantId()));
    }

    @Test
    void doesNotSaveWhenRestaurantIsMissing() {
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.empty());
        assertThrows(RestaurantNotFoundException.class, () -> useCase.createMenuItem(userId, input(BigDecimal.TEN)));
        verifyNoInteractions(items);
    }

    @Test
    void doesNotAccessGatewaysWhenPermissionIsDenied() {
        doThrow(new NotAllowedException()).when(permission).checkCreateMenuItem(userId, restaurantId);
        assertThrows(NotAllowedException.class, () -> useCase.createMenuItem(userId, input(BigDecimal.TEN)));
        verifyNoInteractions(restaurants, items);
    }

    @Test
    void doesNotSaveWhenPriceIsInvalid() {
        restaurantExists();
        for (BigDecimal price : Arrays.asList(null, BigDecimal.ZERO, new BigDecimal("-1"))) {
            assertThrows(MenuItemInvalidPrice.class, () -> useCase.createMenuItem(userId, input(price)));
        }
        verifyNoInteractions(items);
    }
}
