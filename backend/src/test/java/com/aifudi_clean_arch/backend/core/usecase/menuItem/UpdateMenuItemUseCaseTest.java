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

class UpdateMenuItemUseCaseTest {
    private final RestaurantGateway restaurants = mock(RestaurantGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MenuItemGateway items = mock(MenuItemGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final CheckMenuItemPermission permission = mock(CheckMenuItemPermission.class, withSettings().mockMaker("mock-maker-subclass"));
    private final UpdateMenuItemUseCase useCase = new UpdateMenuItemUseCase(restaurants, items, permission);
    private final UUID userId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final MenuItem current = new MenuItem(itemId, "Item", "Descrição", BigDecimal.TEN, true, "old.jpg", restaurantId);

    private UpdateMenuItemInputDto emptyInput() {
        return new UpdateMenuItemInputDto(itemId, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    private void itemExists() {
        when(items.getById(itemId)).thenReturn(Optional.of(current));
    }

    @Test
    void updatesAllFieldsIncludingFalseDeliveryAndRestaurant() {
        itemExists();
        var newRestaurantId = UUID.randomUUID();
        var price = new BigDecimal("35.50");
        when(restaurants.getRestaurantById(newRestaurantId)).thenReturn(Optional.of(new Restaurant(newRestaurantId,
                "Novo restaurante", "Endereço", "09:00", new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of())));
        when(items.update(any())).thenAnswer(i -> i.getArgument(0));
        var input = new UpdateMenuItemInputDto(itemId, Optional.of("Novo item"), Optional.of("Nova descrição"),
                Optional.of(price), Optional.of(false), Optional.of("new.jpg"), Optional.of(newRestaurantId));

        var result = useCase.updateMenuItem(userId, input);

        assertEquals(new MenuItemOutputDto(itemId, "Novo item", "Nova descrição", price, false, "new.jpg", newRestaurantId), result);
        var captor = ArgumentCaptor.forClass(MenuItem.class);
        var order = inOrder(permission, items, restaurants);
        order.verify(permission).checkUpdateMenuItem(userId, itemId);
        order.verify(items).getById(itemId);
        order.verify(restaurants).getRestaurantById(newRestaurantId);
        order.verify(items).update(captor.capture());
        var updated = captor.getValue();
        assertAll(() -> assertEquals(itemId, updated.getId()),
                () -> assertEquals("Novo item", updated.getName()),
                () -> assertEquals("Nova descrição", updated.getDescription()),
                () -> assertEquals(price, updated.getPrice()),
                () -> assertFalse(updated.isHasDelivery()),
                () -> assertEquals("new.jpg", updated.getPicture()),
                () -> assertEquals(newRestaurantId, updated.getRestaurantId()));
    }

    @Test
    void preservesOmittedFieldsAndReturnsDataFromPersistence() {
        itemExists();
        var saved = new MenuItem(itemId, "Nome persistido", "Descrição persistida", new BigDecimal("12.00"),
                false, "saved.jpg", restaurantId);
        when(items.update(any())).thenReturn(saved);

        var result = useCase.updateMenuItem(userId, emptyInput());

        assertEquals(new MenuItemOutputDto(itemId, saved.getName(), saved.getDescription(), saved.getPrice(),
                saved.isHasDelivery(), saved.getPicture(), restaurantId), result);
        var captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(items).update(captor.capture());
        var unchanged = captor.getValue();
        assertAll(() -> assertEquals(current.getId(), unchanged.getId()),
                () -> assertEquals(current.getName(), unchanged.getName()),
                () -> assertEquals(current.getDescription(), unchanged.getDescription()),
                () -> assertEquals(current.getPrice(), unchanged.getPrice()),
                () -> assertEquals(current.isHasDelivery(), unchanged.isHasDelivery()),
                () -> assertEquals(current.getPicture(), unchanged.getPicture()),
                () -> assertEquals(current.getRestaurantId(), unchanged.getRestaurantId()));
        verifyNoInteractions(restaurants);
    }

    @Test
    void doesNotUpdateWhenItemIsMissing() {
        when(items.getById(itemId)).thenReturn(Optional.empty());
        assertThrows(MenuItemNotFountException.class, () -> useCase.updateMenuItem(userId, emptyInput()));
        verify(items, never()).update(any());
        verifyNoInteractions(restaurants);
    }

    @Test
    void doesNotUpdateWhenDestinationRestaurantIsMissing() {
        itemExists();
        var missingId = UUID.randomUUID();
        when(restaurants.getRestaurantById(missingId)).thenReturn(Optional.empty());
        var input = new UpdateMenuItemInputDto(itemId, Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.of(missingId));
        assertThrows(RestaurantNotFoundException.class, () -> useCase.updateMenuItem(userId, input));
        verify(items, never()).update(any());
    }

    @Test
    void doesNotUpdateWhenPriceIsInvalid() {
        itemExists();
        var input = new UpdateMenuItemInputDto(itemId, Optional.empty(), Optional.empty(), Optional.of(BigDecimal.ZERO),
                Optional.empty(), Optional.empty(), Optional.empty());
        assertThrows(MenuItemInvalidPrice.class, () -> useCase.updateMenuItem(userId, input));
        verify(items, never()).update(any());
    }

    @Test
    void doesNotAccessGatewaysWhenPermissionIsDenied() {
        doThrow(new NotAllowedException()).when(permission).checkUpdateMenuItem(userId, itemId);
        assertThrows(NotAllowedException.class, () -> useCase.updateMenuItem(userId, emptyInput()));
        verifyNoInteractions(items, restaurants);
    }
}
