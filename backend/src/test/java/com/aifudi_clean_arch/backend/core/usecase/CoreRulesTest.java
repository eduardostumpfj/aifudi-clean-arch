package com.aifudi_clean_arch.backend.core.usecase;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.dto.membership.MembershipInputDto;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.*;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.GetMenuItemUseCase;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoreRulesTest {
    private final UUID actorId = UUID.randomUUID();
    private final CheckSubRolePermission permission = mock(CheckSubRolePermission.class, withSettings().mockMaker("mock-maker-subclass"));
    private final SubRoleGateway subRoles = mock(SubRoleGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final RoleGateway roles = mock(RoleGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final Role owner = new Role(UUID.randomUUID(), "OWNER", "Owner");
    private final SubRole main = new SubRole(UUID.randomUUID(), "mainOwner", "Main", owner);
    private final SubRole staff = new SubRole(UUID.randomUUID(), "staff", "Staff", owner);

    @Test
    void rejectsRenamingAnotherSubRoleToMain() {
        when(subRoles.getById(staff.getId())).thenReturn(Optional.of(staff));
        var useCase = new UpdateSubRoleUseCase(roles, subRoles, permission);
        assertThrows(ProtectedSubRoleException.class, () -> useCase.update(actorId,
                new UpdateSubRoleInputDto(staff.getId(), Optional.of("mainOwner"), Optional.empty(), Optional.empty())));
        verify(subRoles, never()).update(any());
    }

    @Test
    void rejectsChangingOrDeletingMain() {
        when(subRoles.getById(main.getId())).thenReturn(Optional.of(main));
        assertThrows(ProtectedSubRoleException.class, () -> new UpdateSubRoleUseCase(roles, subRoles, permission).update(actorId,
                new UpdateSubRoleInputDto(main.getId(), Optional.of("other"), Optional.empty(), Optional.empty())));
        assertThrows(ProtectedSubRoleException.class, () -> new DeleteSubRoleUseCase(subRoles, permission).deleteSubRole(actorId, main.getId()));
        verify(subRoles, never()).delete(any());
    }

    @Test
    void reservesMainForAllRolesAndValidatesNullValue() {
        when(roles.getRole(owner.getId())).thenReturn(Optional.of(owner));
        var useCase = new CreateSubRoleUseCase(roles, subRoles, permission);
        assertThrows(ProtectedSubRoleException.class, () -> useCase.createSubRole(actorId,
                new CreateSubRoleInputDto("mainOwner", "Main", owner.getId())));
        assertThrows(IllegalArgumentException.class, () -> useCase.createSubRole(actorId,
                new CreateSubRoleInputDto(null, "Label", owner.getId())));
        verify(subRoles, never()).save(any());
    }

    @Test
    void protectsOwnerMembershipAndPreventsAssigningMain() {
        var memberships = mock(MembershipGateway.class, withSettings().mockMaker("mock-maker-subclass"));
        var restaurants = mock(RestaurantGateway.class, withSettings().mockMaker("mock-maker-subclass"));
        var users = mock(UserGateway.class, withSettings().mockMaker("mock-maker-subclass"));
        var user = new User(UUID.randomUUID(), "User", "u@example.com", "user", "hash", "Address", staff);
        var restaurant = new Restaurant(UUID.randomUUID(), "Restaurant", "Address", "09:00", new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of());
        when(users.getUserById(user.getId())).thenReturn(Optional.of(user));
        when(restaurants.getRestaurantById(restaurant.getId())).thenReturn(Optional.of(restaurant));
        when(subRoles.getById(staff.getId())).thenReturn(Optional.of(staff));
        when(subRoles.getById(main.getId())).thenReturn(Optional.of(main));
        when(memberships.getUserRestaurantMembership(user.getId(), restaurant.getId()))
                .thenReturn(Optional.of(new Membership(UUID.randomUUID(), user, main, restaurant)));
        var useCase = new AssignSubRoleToUserUseCase(memberships, subRoles, restaurants, users, permission);
        assertThrows(ProtectedSubRoleException.class, () -> useCase.assignSubRoleToUser(actorId, new MembershipInputDto(user.getId(), staff.getId(), restaurant.getId())));
        assertThrows(ProtectedSubRoleException.class, () -> useCase.assignSubRoleToUser(actorId, new MembershipInputDto(user.getId(), main.getId(), restaurant.getId())));
        verify(memberships, never()).save(any());
    }

    @Test
    void getsItemWithItsRestaurantAndReportsMissingItem() {
        var gateway = mock(MenuItemGateway.class, withSettings().mockMaker("mock-maker-subclass"));
        var item = new MenuItem(UUID.randomUUID(), "Item", "Description", new BigDecimal("19.90"), true, "picture", UUID.randomUUID());
        when(gateway.getById(item.getId())).thenReturn(Optional.of(item));
        var useCase = new GetMenuItemUseCase(gateway, mock(com.aifudi_clean_arch.backend.core.usecase.menuItem.CheckMenuItemPermission.class, withSettings().mockMaker("mock-maker-subclass")));
        assertEquals(item.getRestaurantId(), useCase.getMenuItemById(actorId, item.getId()).restaurantId());
        assertThrows(MenuItemNotFountException.class, () -> useCase.getMenuItemById(actorId, UUID.randomUUID()));
    }

    @Test
    void rejectsInvalidPricesAndRequiredItemFields() {
        var id = UUID.randomUUID();
        assertThrows(MenuItemInvalidPrice.class, () -> new MenuItem(id, "Item", "", BigDecimal.ZERO, false, "", id));
        assertThrows(MenuItemInvalidPrice.class, () -> new MenuItem(id, "Item", "", new BigDecimal("-1"), false, "", id));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem(id, " ", "", BigDecimal.ONE, false, "", id));
        assertThrows(IllegalArgumentException.class, () -> new MenuItem(id, "Item", "", BigDecimal.ONE, false, "", null));
    }
}
