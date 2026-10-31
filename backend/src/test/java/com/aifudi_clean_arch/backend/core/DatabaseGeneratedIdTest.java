package com.aifudi_clean_arch.backend.core;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.dto.restaurant.CreateRestaurantInputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.CreateMenuItemInputDto;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.*;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.Supplier;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseGeneratedIdTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }

    @Test
    void restaurantUsesSavedIdForOwnerMembershipAndResponse() {
        var restaurants = mockType(RestaurantGateway.class);
        var users = mockType(UserGateway.class);
        var subroles = mockType(SubRoleGateway.class);
        var kitchens = mockType(KitchenGateway.class);
        var memberships = mockType(MembershipGateway.class);
        var transactions = mockType(TransactionGateway.class);
        var permission = mockType(CheckRestaurantPermission.class);
        var role = new Role(UUID.randomUUID(), "OWNER", "Owner");
        var subrole = new SubRole(UUID.randomUUID(), "main", "Main", role);
        var owner = new User(UUID.randomUUID(), "Owner", "a@b.com", "owner", "hash", "Address", subrole);
        var kitchen = new Kitchen(UUID.randomUUID(), "type", "Type");
        var generatedId = UUID.randomUUID();
        when(users.getUserById(owner.getId())).thenReturn(Optional.of(owner));
        when(kitchens.getKitchenById(kitchen.getId())).thenReturn(Optional.of(kitchen));
        when(subroles.getByValue("main")).thenReturn(Optional.of(subrole));
        when(transactions.execute(any())).thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
        when(restaurants.save(any())).thenAnswer(i -> {
            Restaurant pending = i.getArgument(0);
            assertNull(pending.getId());
            return new Restaurant(generatedId, pending.getName(), pending.getAddress(),
                    pending.getOpeningTime(), kitchen, Set.of(), Set.of());
        });
        var result = new CreateRestaurantUseCase(restaurants, users, subroles, kitchens,
                memberships, transactions, permission).createRestaurant(owner.getId(),
                new CreateRestaurantInputDto(owner.getId(), "Restaurant", "Address", "09:00", kitchen.getId()));
        assertEquals(generatedId, result.id());
        var order = inOrder(restaurants, memberships);
        order.verify(restaurants).save(any());
        order.verify(memberships).save(argThat(m -> m.getId() == null
                && generatedId.equals(m.getRestaurant().getId())));
    }

    @Test
    void menuItemResponseUsesIdReturnedByPersistence() {
        var restaurants = mockType(RestaurantGateway.class);
        var items = mockType(MenuItemGateway.class);
        var permission = mockType(CheckMenuItemPermission.class);
        var restaurant = new Restaurant(UUID.randomUUID(), "Restaurant", "Address", "09:00",
                new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of());
        var generatedId = UUID.randomUUID();
        when(restaurants.getRestaurantById(restaurant.getId())).thenReturn(Optional.of(restaurant));
        when(items.save(any())).thenAnswer(i -> {
            MenuItem pending = i.getArgument(0);
            assertNull(pending.getId());
            return new MenuItem(generatedId, pending.getName(), pending.getDescription(),
                    pending.getPrice(), pending.isHasDelivery(), pending.getPicture(), restaurant.getId());
        });
        var result = new CreateMenuItemUseCase(restaurants, items, permission).createMenuItem(UUID.randomUUID(),
                new CreateMenuItemInputDto("Item", "Description", BigDecimal.TEN, true, "picture", restaurant.getId()));
        assertEquals(generatedId, result.id());
    }
}
