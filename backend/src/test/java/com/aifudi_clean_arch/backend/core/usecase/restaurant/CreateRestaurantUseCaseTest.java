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

class CreateRestaurantUseCaseTest {
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
    private final UserGateway users = mockType(UserGateway.class);
    private final SubRoleGateway subroles = mockType(SubRoleGateway.class);
    private final KitchenGateway kitchens = mockType(KitchenGateway.class);
    private final MembershipGateway memberships = mockType(MembershipGateway.class);
    private final TransactionGateway transactions = mockType(TransactionGateway.class);
    private final CreateRestaurantUseCase useCase = new CreateRestaurantUseCase(restaurants, users, subroles,
            kitchens, memberships, transactions, permission);
    private final SubRole ownerRole = new SubRole(UUID.randomUUID(), "mainOwner", "Principal", new Role(UUID.randomUUID(), "OWNER", "Proprietário"));
    private final User owner = new User(UUID.randomUUID(), "Dono", "owner@example.com", "owner", "hash", "Endereço", ownerRole);

    private CreateRestaurantInputDto input() {
        return new CreateRestaurantInputDto(owner.getId(), "Restaurante", "Endereço", "11:00 - 22:00", kitchen.getId());
    }
    private void dependenciesExist() {
        when(users.getUserById(owner.getId())).thenReturn(Optional.of(owner));
        when(kitchens.getKitchenById(kitchen.getId())).thenReturn(Optional.of(kitchen));
        when(subroles.getByValue("mainOwner")).thenReturn(Optional.of(ownerRole));
    }
    private void executeTransaction() {
        when(transactions.execute(any())).thenAnswer(i -> ((Supplier<?>) i.getArgument(0)).get());
    }

    @Test
    void createsRestaurantAndOwnerMembershipInsideTransaction() {
        dependenciesExist();
        executeTransaction();
        var saved = restaurant();
        when(restaurants.save(any())).thenReturn(saved);

        var result = useCase.createRestaurant(userId, input());

        assertEquals(new CreateRestaurantOutputDto(restaurantId, saved.getName(), saved.getAddress(),
                saved.getOpeningTime(), kitchen.getId(), owner.getId()), result);
        var pending = ArgumentCaptor.forClass(Restaurant.class);
        var link = ArgumentCaptor.forClass(Membership.class);
        var order = inOrder(permission, transactions, restaurants, memberships);
        order.verify(permission).checkCreateRestaurant(userId);
        order.verify(transactions).execute(any());
        order.verify(restaurants).save(pending.capture());
        order.verify(memberships).save(link.capture());
        var created = pending.getValue();
        assertAll(() -> assertNull(created.getId()),
                () -> assertEquals("Restaurante", created.getName()),
                () -> assertEquals("Endereço", created.getAddress()),
                () -> assertEquals("11:00 - 22:00", created.getOpeningTime()),
                () -> assertSame(kitchen, created.getKitchen()),
                () -> assertTrue(created.getMembers().isEmpty()),
                () -> assertTrue(created.getMenuItems().isEmpty()),
                () -> assertNull(link.getValue().getId()),
                () -> assertSame(owner, link.getValue().getUser()),
                () -> assertSame(ownerRole, link.getValue().getSubrole()),
                () -> assertSame(saved, link.getValue().getRestaurant()));
    }

    @Test
    void rejectsRequestBeforeAccessingGatewaysWhenPermissionIsDenied() {
        doThrow(new NotAllowedException()).when(permission).checkCreateRestaurant(userId);
        assertThrows(NotAllowedException.class, () -> useCase.createRestaurant(userId, input()));
        verifyNoInteractions(users, kitchens, subroles, restaurants, memberships, transactions);
    }

    @Test
    void rejectsMissingOwner() {
        when(users.getUserById(owner.getId())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> useCase.createRestaurant(userId, input()));
        verifyNoInteractions(kitchens, subroles, restaurants, memberships, transactions);
    }

    @Test
    void rejectsMissingKitchen() {
        when(users.getUserById(owner.getId())).thenReturn(Optional.of(owner));
        when(kitchens.getKitchenById(kitchen.getId())).thenReturn(Optional.empty());
        assertThrows(KitchenNotFoundException.class, () -> useCase.createRestaurant(userId, input()));
        verifyNoInteractions(subroles, restaurants, memberships, transactions);
    }

    @Test
    void rejectsMissingOwnerSubrole() {
        dependenciesExist();
        when(subroles.getByValue("mainOwner")).thenReturn(Optional.empty());
        assertThrows(SubRoleNotFoundException.class, () -> useCase.createRestaurant(userId, input()));
        verifyNoInteractions(restaurants, memberships, transactions);
    }

    @Test
    void propagatesMembershipFailureFromTransaction() {
        dependenciesExist();
        executeTransaction();
        when(restaurants.save(any())).thenReturn(restaurant());
        var failure = new IllegalStateException("Falha ao salvar vínculo");
        when(memberships.save(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> useCase.createRestaurant(userId, input())));
    }

    @Test
    void doesNotSaveMembershipWhenRestaurantPersistenceFails() {
        dependenciesExist();
        executeTransaction();
        var failure = new IllegalStateException("Falha ao salvar restaurante");
        when(restaurants.save(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> useCase.createRestaurant(userId, input())));
        verifyNoInteractions(memberships);
    }
}
