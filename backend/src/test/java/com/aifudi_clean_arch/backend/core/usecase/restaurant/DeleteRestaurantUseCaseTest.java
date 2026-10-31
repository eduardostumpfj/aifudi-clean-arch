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

class DeleteRestaurantUseCaseTest {
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
    private final DeleteRestaurantUseCase useCase = new DeleteRestaurantUseCase(restaurants, permission);

    @Test
    void deletesRestaurantAfterCheckingPermission() {
        when(restaurants.delete(restaurantId)).thenReturn(true);
        assertDoesNotThrow(() -> useCase.deleteRestaurant(userId, restaurantId));
        var order = inOrder(permission, restaurants);
        order.verify(permission).checkDeleteRestaurant(userId, restaurantId);
        order.verify(restaurants).delete(restaurantId);
    }

    @Test
    void throwsWhenRestaurantWasNotDeleted() {
        when(restaurants.delete(restaurantId)).thenReturn(false);
        assertThrows(RestaurantNotFoundException.class, () -> useCase.deleteRestaurant(userId, restaurantId));
        verify(restaurants).delete(restaurantId);
    }

    @Test
    void doesNotDeleteWhenPermissionIsDenied() {
        doThrow(new NotAllowedException()).when(permission).checkDeleteRestaurant(userId, restaurantId);
        assertThrows(NotAllowedException.class, () -> useCase.deleteRestaurant(userId, restaurantId));
        verifyNoInteractions(restaurants);
    }
}
