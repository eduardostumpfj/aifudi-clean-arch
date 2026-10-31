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

class GetRestaurantUseCaseTest {
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
    private final GetRestaurantUseCase useCase = new GetRestaurantUseCase(restaurants, permission);

    @Test
    void returnsRestaurantWithMembershipAndMenuItemIds() {
        var subrole = new SubRole(UUID.randomUUID(), "mainOwner", "Principal", new Role(UUID.randomUUID(), "OWNER", "Dono"));
        var user = new User(userId, "Dono", "owner@example.com", "owner", "hash", "Endereço", subrole);
        var membership = new Membership(UUID.randomUUID(), user, subrole, restaurant());
        var item = new MenuItem(UUID.randomUUID(), "Item", "Descrição", BigDecimal.TEN, true, "item.jpg", restaurantId);
        var found = new Restaurant(restaurantId, "Restaurante", "Endereço", "11:00 - 22:00", kitchen, Set.of(membership), Set.of(item));
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.of(found));

        var result = useCase.getRestaurant(userId, restaurantId);

        assertEquals(new GetRestaurantOutputDto(restaurantId, found.getName(), found.getAddress(), found.getOpeningTime(),
                kitchen.getId(), Set.of(membership.getId()), Set.of(item.getId())), result);
        var order = inOrder(permission, restaurants);
        order.verify(permission).checkGetRestaurant(userId);
        order.verify(restaurants).getRestaurantById(restaurantId);
    }

    @Test
    void returnsEmptyCollectionsForRestaurantWithoutMembersOrItems() {
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant()));
        var result = useCase.getRestaurant(userId, restaurantId);
        assertTrue(result.membershipIds().isEmpty());
        assertTrue(result.menuItemsIdList().isEmpty());
    }

    @Test
    void throwsWhenRestaurantIsMissing() {
        when(restaurants.getRestaurantById(restaurantId)).thenReturn(Optional.empty());
        assertThrows(RestaurantNotFoundException.class, () -> useCase.getRestaurant(userId, restaurantId));
    }

    @Test
    void doesNotQueryRestaurantWhenPermissionIsDenied() {
        doThrow(new NotAllowedException()).when(permission).checkGetRestaurant(userId);
        assertThrows(NotAllowedException.class, () -> useCase.getRestaurant(userId, restaurantId));
        verifyNoInteractions(restaurants);
    }
}
