package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.MenuItem;
import com.aifudi_clean_arch.backend.core.exception.ErrorOnRepositoryAccess;
import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.MenuItemEntity;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.RestaurantEntity;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.MenuItemRepository;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.RestaurantRepository;
import com.aifudi_clean_arch.backend.infra.database.mapper.MenuItemMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuItemJpaGatewayTest {
    private final MenuItemRepository repository = mock(MenuItemRepository.class, withSettings().mockMaker("mock-maker-subclass"));
    private final RestaurantRepository restaurants = mock(RestaurantRepository.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MenuItemJpaGateway gateway = new MenuItemJpaGateway(repository, restaurants, new MenuItemMapper());
    private final UUID id = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();

    private MenuItem item() {
        return new MenuItem(id, "Item", "Description", new BigDecimal("19.90"), true, "picture", restaurantId);
    }

    private RestaurantEntity restaurant() {
        var restaurant = new RestaurantEntity();
        restaurant.setId(restaurantId);
        return restaurant;
    }

    @Test
    void savesAllFieldsIncludingRestaurant() {
        when(restaurants.getReferenceById(restaurantId)).thenReturn(restaurant());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(id, gateway.save(item()).getId());
        verify(repository).save(argThat(entity -> entity.getId().equals(id)
                && entity.getRestaurant().getId().equals(restaurantId)
                && entity.getPrice().equals(new BigDecimal("19.90"))));
    }

    @Test
    void returnsEmptyWhenMissingAndMapsFoundItem() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertTrue(gateway.getById(id).isEmpty());
        var entity = new MenuItemMapper().map(item(), restaurant());
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        assertEquals(restaurantId, gateway.getById(id).orElseThrow().getRestaurantId());
    }

    @Test
    void deleteReportsActualAffectedRows() {
        when(repository.deleteByIdReturningCount(id)).thenReturn(0, 1);
        assertFalse(gateway.delete(id));
        assertTrue(gateway.delete(id));
    }

    @Test
    void updateDoesNotInsertMissingItem() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(MenuItemNotFountException.class, () -> gateway.update(item()));
        verify(repository, never()).save(any());
        verifyNoInteractions(restaurants);
    }

    @Test
    void updatePersistsAllFieldsIncludingRestaurantTransfer() {
        var oldRestaurant = restaurant();
        oldRestaurant.setId(UUID.randomUUID());
        var current = new MenuItemEntity(id, oldRestaurant, "Old", "Old", BigDecimal.ONE, "old", false);
        when(repository.findById(id)).thenReturn(Optional.of(current));
        when(restaurants.getReferenceById(restaurantId)).thenReturn(restaurant());
        when(repository.save(current)).thenReturn(current);
        var updated = gateway.update(item());
        assertEquals("Item", updated.getName());
        assertEquals("Description", updated.getDescription());
        assertEquals(new BigDecimal("19.90"), updated.getPrice());
        assertTrue(updated.isHasDelivery());
        assertEquals("picture", updated.getPicture());
        assertEquals(restaurantId, updated.getRestaurantId());
        verify(repository).save(current);
    }

    @Test
    void preservesPersistenceFailureCause() {
        var failure = new DataAccessResourceFailureException("Database unavailable");
        when(repository.findById(id)).thenThrow(failure);
        var exception = assertThrows(ErrorOnRepositoryAccess.class, () -> gateway.getById(id));
        assertSame(failure, exception.getCause());
    }
}
