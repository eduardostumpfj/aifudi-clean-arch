package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.*;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.*;
import com.aifudi_clean_arch.backend.infra.database.mapper.*;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantJpaGatewayTest {
    private final RestaurantRepository repository = stub(RestaurantRepository.class);
    private final KitchenRepository kitchens = stub(KitchenRepository.class);
    private final MembershipRepository memberships = stub(MembershipRepository.class);
    private final MenuItemRepository items = stub(MenuItemRepository.class);
    private final RestaurantMapper mapper = new RestaurantMapper(new KitchenMapper());
    private final SubRoleMapper subRoleMapper = new SubRoleMapper(new RoleMapper());
    private final MembershipMapper membershipMapper = new MembershipMapper(new UserMapper(subRoleMapper), subRoleMapper, mapper);
    private final RestaurantJpaGateway gateway = new RestaurantJpaGateway(repository, kitchens, memberships, items, mapper, membershipMapper, new MenuItemMapper());
    private final UUID id = UUID.randomUUID();
    private final KitchenEntity kitchen = new KitchenEntity(UUID.randomUUID(), "type", "Type");
    private final RestaurantEntity entity = new RestaurantEntity(id, kitchen, "Name", "Address", "09:00");

    private static <T> T stub(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }

    @Test
    void loadsMembersAndMenuWithoutRecursiveExpansion() {
        var role = new RoleEntity(UUID.randomUUID(), "OWNER", "Owner");
        var subRole = new SubRoleEntity(UUID.randomUUID(), role, "mainOwner", "Main");
        var user = new UserEntity(UUID.randomUUID(), subRole, "User", "login", "email", "hash", "Address");
        var member = new MembershipEntity(UUID.randomUUID(), user, subRole, entity);
        var item = new MenuItemEntity(UUID.randomUUID(), entity, "Item", "Desc", BigDecimal.ONE, "picture", false);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(memberships.findByRestaurant_Id(id)).thenReturn(List.of(member));
        when(items.findByRestaurant_Id(id)).thenReturn(List.of(item));
        var result = gateway.getRestaurantById(id).orElseThrow();
        assertEquals(member.getId(), result.getMembers().iterator().next().getId());
        assertEquals(item.getId(), result.getMenuItems().iterator().next().getId());
        assertTrue(result.getMembers().iterator().next().getRestaurant().getMembers().isEmpty());
    }

    @Test
    void missingRestaurantDoesNotLoadCollections() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertTrue(gateway.getRestaurantById(id).isEmpty());
        verifyNoInteractions(memberships, items);
    }

    @Test
    void savesRestaurantWithoutCascadingCollections() {
        var restaurant = mapper.map(entity);
        when(kitchens.getReferenceById(kitchen.getId())).thenReturn(kitchen);
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        assertEquals(id, gateway.save(restaurant).getId());
        verifyNoInteractions(memberships, items);
    }

    @Test
    void updatePersistsFieldsAndKitchenAndRejectsMissingRestaurant() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RestaurantNotFoundException.class, () -> gateway.update(mapper.map(entity)));
        verify(repository, never()).save(any());
        var newKitchen = new KitchenEntity(UUID.randomUUID(), "other", "Other");
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(kitchens.getReferenceById(newKitchen.getId())).thenReturn(newKitchen);
        when(repository.save(entity)).thenReturn(entity);
        var updated = new Restaurant(id, "New", "New address", "10:00", new KitchenMapper().map(newKitchen), Set.of(), Set.of());
        var result = gateway.update(updated);
        assertEquals("New", result.getName());
        assertEquals("New address", result.getAddress());
        assertEquals("10:00", result.getOpeningTime());
        assertEquals(newKitchen.getId(), result.getKitchen().getId());
    }

    @Test
    void deleteReportsAffectedRowsAndPreservesFailureCause() {
        when(repository.deleteByIdReturningCount(id)).thenReturn(0, 1);
        assertFalse(gateway.delete(id));
        assertTrue(gateway.delete(id));
        var failure = new DataAccessResourceFailureException("offline");
        when(repository.deleteByIdReturningCount(id)).thenThrow(failure);
        assertSame(failure, assertThrows(ErrorOnRepositoryAccess.class, () -> gateway.delete(id)).getCause());
    }

    @Test
    void saveReturnsGeneratedIdAndPreservesDomainCollections() {
        var generatedId = UUID.randomUUID();
        var domain = mapper.map(entity);
        var pending = new Restaurant(null, domain.getName(), domain.getAddress(), domain.getOpeningTime(),
                domain.getKitchen(), domain.getMembers(), domain.getMenuItems());
        when(kitchens.getReferenceById(kitchen.getId())).thenReturn(kitchen);
        when(repository.save(any())).thenAnswer(i -> {
            RestaurantEntity saved = i.getArgument(0);
            assertNull(saved.getId());
            saved.setId(generatedId);
            return saved;
        });
        var saved = gateway.save(pending);
        assertAll(() -> assertEquals(generatedId, saved.getId()),
                () -> assertEquals(pending.getName(), saved.getName()),
                () -> assertEquals(pending.getAddress(), saved.getAddress()),
                () -> assertEquals(pending.getOpeningTime(), saved.getOpeningTime()),
                () -> assertSame(pending.getKitchen(), saved.getKitchen()),
                () -> assertSame(pending.getMembers(), saved.getMembers()),
                () -> assertSame(pending.getMenuItems(), saved.getMenuItems()));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"get", "save", "update"})
    void wrapsRepositoryFailuresAndPreservesCause(String operation) {
        var failure = new DataAccessResourceFailureException("offline");
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(kitchens.getReferenceById(kitchen.getId())).thenReturn(kitchen);
        when(repository.save(any())).thenThrow(failure);
        if (operation.equals("get")) when(repository.findById(id)).thenThrow(failure);
        var error = assertThrows(ErrorOnRepositoryAccess.class, () -> {
            switch (operation) {
                case "get" -> gateway.getRestaurantById(id);
                case "save" -> gateway.save(mapper.map(entity));
                default -> gateway.update(mapper.map(entity));
            }
        });
        assertSame(failure, error.getCause());
    }
}
