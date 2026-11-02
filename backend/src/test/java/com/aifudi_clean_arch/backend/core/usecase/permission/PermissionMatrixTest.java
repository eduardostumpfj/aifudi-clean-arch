package com.aifudi_clean_arch.backend.core.usecase.permission;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.exception.NotAllowedException;
import com.aifudi_clean_arch.backend.core.exception.MenuItemNotFountException;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.permission.RestaurantPermissionChecker;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.CheckRestaurantPermission;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CheckSubRolePermission;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.CheckMenuItemPermission;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PermissionMatrixTest {
    private final UserGateway users = mock(UserGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MembershipGateway memberships = mock(MembershipGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MenuItemGateway items = mock(MenuItemGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final RestaurantPermissionChecker checker = new RestaurantPermissionChecker(users, memberships);
    private final CheckRestaurantPermission restaurants = new CheckRestaurantPermission(checker);
    private final CheckSubRolePermission types = new CheckSubRolePermission(checker);
    private final CheckMenuItemPermission menu = new CheckMenuItemPermission(new com.aifudi_clean_arch.backend.core.usecase.permission.MenuItemPermissionChecker(checker, items));

    private User user(String role) {
        var subrole = new SubRole(UUID.randomUUID(), "member", "Member", new Role(UUID.randomUUID(), role, role));
        var user = new User(UUID.randomUUID(), "Name", "u@example.com", "login", "hash", "Address", subrole);
        when(users.getUserById(user.getId())).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void readCreateAndTypePermissionsFollowRoleMatrix() {
        for (String role : List.of("ADMIN", "OWNER", "USER")) {
            var id = user(role).getId();
            assertDoesNotThrow(() -> restaurants.checkGetRestaurant(id));
            assertDoesNotThrow(() -> menu.checkGetMenuItem(id));
            if (role.equals("USER")) {
                assertThrows(NotAllowedException.class, () -> restaurants.checkCreateRestaurant(id));
            } else {
                assertDoesNotThrow(() -> restaurants.checkCreateRestaurant(id));
            }
            if (role.equals("ADMIN")) {
                assertDoesNotThrow(() -> types.checkCreateSubRole(id));
                assertDoesNotThrow(() -> types.checkUpdateSubRole(id));
                assertDoesNotThrow(() -> types.checkDeleteSubRole(id));
                assertDoesNotThrow(() -> types.checkGetSubRole(id));
                assertDoesNotThrow(() -> types.checkAssignSubRole(id, UUID.randomUUID()));
            } else {
                assertThrows(NotAllowedException.class, () -> types.checkCreateSubRole(id));
                assertThrows(NotAllowedException.class, () -> types.checkUpdateSubRole(id));
                assertThrows(NotAllowedException.class, () -> types.checkDeleteSubRole(id));
                assertThrows(NotAllowedException.class, () -> types.checkGetSubRole(id));
                assertThrows(NotAllowedException.class, () -> types.checkAssignSubRole(id, UUID.randomUUID()));
            }
        }
    }

    @Test
    void adminManagesAnyRestaurantWithoutMembership() {
        var admin = user("ADMIN").getId();
        var restaurantId = UUID.randomUUID();
        assertDoesNotThrow(() -> restaurants.checkUpdateRestaurant(admin, restaurantId));
        assertDoesNotThrow(() -> restaurants.checkDeleteRestaurant(admin, restaurantId));
        assertDoesNotThrow(() -> menu.checkCreateMenuItem(admin, restaurantId));
        verifyNoInteractions(memberships);
    }

    @Test
    void ownerCanOnlyManageItemsAndRestaurantsOfTheirMembership() {
        var owner = user("OWNER");
        var restaurant = new Restaurant(UUID.randomUUID(), "Restaurant", "Address", "09:00",
                new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of());
        when(memberships.getUserRestaurantMembership(owner.getId(), restaurant.getId()))
                .thenReturn(Optional.of(new Membership(UUID.randomUUID(), owner, owner.getClientSubrole(), restaurant)));
        var itemId = UUID.randomUUID();
        when(items.getById(itemId)).thenReturn(Optional.of(new MenuItem(itemId, "Item", "Description",
                BigDecimal.TEN, false, "picture", restaurant.getId())));
        assertDoesNotThrow(() -> menu.checkUpdateMenuItem(owner.getId(), itemId));
        assertDoesNotThrow(() -> menu.checkDeleteMenuItem(owner.getId(), itemId));
        assertDoesNotThrow(() -> restaurants.checkDeleteRestaurant(owner.getId(), restaurant.getId()));
        assertThrows(NotAllowedException.class, () -> restaurants.checkUpdateRestaurant(owner.getId(), UUID.randomUUID()));
        var client = user("USER");
        when(memberships.getUserRestaurantMembership(client.getId(), restaurant.getId()))
                .thenReturn(Optional.of(new Membership(UUID.randomUUID(), client, client.getClientSubrole(), restaurant)));
        assertThrows(NotAllowedException.class, () -> menu.checkUpdateMenuItem(client.getId(), itemId));
    }

    @Test
    void rejectsMissingIdentityAndUnknownRole() {
        assertThrows(NotAllowedException.class, () -> restaurants.checkGetRestaurant(null));
        assertThrows(NotAllowedException.class, () -> restaurants.checkGetRestaurant(UUID.randomUUID()));
        var unknown = user("OTHER").getId();
        assertThrows(NotAllowedException.class, () -> menu.checkGetMenuItem(unknown));
    }
    @Test
    void rejectsMissingRestaurantBeforeLookingUpMembership() {
        var owner = user("OWNER");

        assertThrows(NotAllowedException.class,
                () -> checker.checkManageRestaurant(owner.getId(), null));

        verifyNoInteractions(memberships);
    }

    @Test
    void reportsMissingMenuItemBeforeCheckingRestaurantPermission() {
        var userId = UUID.randomUUID();
        var itemId = UUID.randomUUID();
        when(items.getById(itemId)).thenReturn(Optional.empty());

        assertThrows(MenuItemNotFountException.class,
                () -> menu.checkUpdateMenuItem(userId, itemId));

        verify(items).getById(itemId);
        verifyNoInteractions(users, memberships);
    }

    @Test
    void onlyMainOwnerCanAssignWithinTheirRestaurantAndAdminBypassesMembership() {
        var actor = user("OWNER");
        var restaurant = new Restaurant(UUID.randomUUID(), "Restaurant", "Address", "09:00",
                new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of());
        var main = new SubRole(UUID.randomUUID(), "mainOwner", "Principal",
                new Role(UUID.randomUUID(), "OWNER", "Owner"));
        when(memberships.getUserRestaurantMembership(actor.getId(), restaurant.getId()))
                .thenReturn(Optional.of(new Membership(UUID.randomUUID(), actor, main, restaurant)));
        assertFalse(types.checkAssignSubRole(actor.getId(), restaurant.getId()));
        assertThrows(NotAllowedException.class,
                () -> types.checkAssignSubRole(actor.getId(), UUID.randomUUID()));
        when(memberships.getUserRestaurantMembership(actor.getId(), restaurant.getId()))
                .thenReturn(Optional.of(new Membership(UUID.randomUUID(), actor, actor.getClientSubrole(), restaurant)));
        assertThrows(NotAllowedException.class,
                () -> types.checkAssignSubRole(actor.getId(), restaurant.getId()));
        assertThrows(NotAllowedException.class, () -> types.checkAssignSubRole(actor.getId(), null));
        assertTrue(types.checkAssignSubRole(user("ADMIN").getId(), UUID.randomUUID()));
    }
}
