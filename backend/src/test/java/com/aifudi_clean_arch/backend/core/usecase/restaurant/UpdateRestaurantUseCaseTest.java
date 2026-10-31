package com.aifudi_clean_arch.backend.core.usecase.restaurant;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateRestaurantUseCaseTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final RestaurantGateway restaurants = mockType(RestaurantGateway.class);
    private final CheckRestaurantPermission permission = mockType(CheckRestaurantPermission.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final Kitchen kitchen = new Kitchen(UUID.randomUUID(), "BRAZILIAN", "Brasileira");

    private Restaurant restaurant() {
        return new Restaurant(restaurantId, "Restaurante", "Endereço", "11:00 - 22:00", kitchen, Set.of(), Set.of());
    }
    private final KitchenGateway kitchens = mockType(KitchenGateway.class);
    private final UpdateRestaurantUseCase useCase = new UpdateRestaurantUseCase(restaurants, kitchens, permission);

    private UpdateRestaurantInputDto emptyInput() {
        return new UpdateRestaurantInputDto(restaurantId, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    @Test
    void updatesAllFieldsAndKitchen() {
        var current = restaurant();
        var newKitchen = new Kitchen(UUID.randomUUID(), "ITALIAN", "Italiana");
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.of(current));
        when(kitchens.getKitchenById(newKitchen.getId())).thenReturn(Optional.of(newKitchen));
        when(restaurants.update(any())).thenAnswer(i -> i.getArgument(0));
        var input = new UpdateRestaurantInputDto(restaurantId, Optional.of("Novo nome"), Optional.of("Novo endereço"),
                Optional.of("18:00 - 23:00"), Optional.of(newKitchen.getId()));

        var result = useCase.updateRestaurant(userId, input);

        assertEquals(new UpdateRestaurantOutputDto(restaurantId, "Novo nome", "Novo endereço", "18:00 - 23:00", newKitchen.getId()), result);
        var captor = ArgumentCaptor.forClass(Restaurant.class);
        var order = inOrder(permission, restaurants, kitchens);
        order.verify(permission).checkUpdateRestaurant(userId, restaurantId);
        order.verify(restaurants).getRestaurantById(restaurantId);
        order.verify(kitchens).getKitchenById(newKitchen.getId());
        order.verify(restaurants).update(captor.capture());
        var updated = captor.getValue();
        assertAll(() -> assertEquals(restaurantId, updated.getId()),
                () -> assertEquals("Novo nome", updated.getName()),
                () -> assertEquals("Novo endereço", updated.getAddress()),
                () -> assertEquals("18:00 - 23:00", updated.getOpeningTime()),
                () -> assertSame(newKitchen, updated.getKitchen()),
                () -> assertSame(current.getMembers(), updated.getMembers()),
                () -> assertSame(current.getMenuItems(), updated.getMenuItems()));
    }

    @Test
    void preservesOmittedFieldsAndReturnsPersistedData() {
        var current = restaurant();
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.of(current));
        var saved = new Restaurant(restaurantId, "Nome persistido", "Endereço persistido", "09:00", kitchen, Set.of(), Set.of());
        when(restaurants.update(any())).thenReturn(saved);

        var result = useCase.updateRestaurant(userId, emptyInput());

        assertEquals(new UpdateRestaurantOutputDto(restaurantId, saved.getName(), saved.getAddress(), saved.getOpeningTime(), kitchen.getId()), result);
        var captor = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurants).update(captor.capture());
        var unchanged = captor.getValue();
        assertAll(() -> assertEquals(current.getName(), unchanged.getName()),
                () -> assertEquals(current.getAddress(), unchanged.getAddress()),
                () -> assertEquals(current.getOpeningTime(), unchanged.getOpeningTime()),
                () -> assertSame(kitchen, unchanged.getKitchen()),
                () -> assertSame(current.getMembers(), unchanged.getMembers()),
                () -> assertSame(current.getMenuItems(), unchanged.getMenuItems()));
        verifyNoInteractions(kitchens);
    }

    @Test
    void rejectsMissingRestaurant() {
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.empty());
        assertThrows(RestaurantNotFoundException.class, () -> useCase.updateRestaurant(userId, emptyInput()));
        verify(restaurants, never()).update(any());
        verifyNoInteractions(kitchens);
    }

    @Test
    void rejectsMissingKitchen() {
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant()));
        var missingId = UUID.randomUUID();
        when(kitchens.getKitchenById(missingId)).thenReturn(Optional.empty());
        var input = new UpdateRestaurantInputDto(restaurantId, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(missingId));
        assertThrows(KitchenNotFoundException.class, () -> useCase.updateRestaurant(userId, input));
        verify(restaurants, never()).update(any());
    }

    @Test
    void rejectsInvalidRestaurantFieldsBeforeSaving() {
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant()));
        var input = new UpdateRestaurantInputDto(restaurantId, Optional.of(" "), Optional.empty(), Optional.empty(), Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> useCase.updateRestaurant(userId, input));
        verify(restaurants, never()).update(any());
    }

    @Test
    void doesNotAccessGatewaysWhenPermissionIsDenied() {
        doThrow(new NotAllowedException()).when(permission).checkUpdateRestaurant(userId, restaurantId);
        assertThrows(NotAllowedException.class, () -> useCase.updateRestaurant(userId, emptyInput()));
        verifyNoInteractions(restaurants, kitchens);
    }
}
