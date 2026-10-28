package com.aifudi_clean_arch.backend.core;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.dto.membership.MembershipInputDto;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.exception.NotAllowedException;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.permission.RestaurantPermissionChecker;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubRoleFlowTest {
    private final UserGateway users = mock(UserGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MembershipGateway memberships = mock(MembershipGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final SubRoleGateway subroles = mock(SubRoleGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final RoleGateway roles = mock(RoleGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final RestaurantGateway restaurants = mock(RestaurantGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final CheckSubRolePermission permission = new CheckSubRolePermission(new RestaurantPermissionChecker(users, memberships));

    private User user(String role) {
        var subrole = new SubRole(UUID.randomUUID(), "member", "Member", new Role(UUID.randomUUID(), role, role));
        var user = new User(UUID.randomUUID(), "User", "u@example.com", "user", "hash", "Address", subrole);
        when(users.getUserById(user.getId())).thenReturn(Optional.of(user));
        return user;
    }

    @Test
    void rejectsNonAdminsBeforeAccessingResourcesInEveryUseCase() {
        for (String role : List.of("OWNER", "USER")) {
            var actor = user(role).getId();
            assertThrows(NotAllowedException.class, () -> new CreateSubRoleUseCase(roles, subroles, permission)
                    .createSubRole(actor, new CreateSubRoleInputDto("staff", "Staff", UUID.randomUUID())));
            assertThrows(NotAllowedException.class, () -> new UpdateSubRoleUseCase(roles, subroles, permission)
                    .update(actor, new UpdateSubRoleInputDto(UUID.randomUUID(), Optional.empty(), Optional.empty(), Optional.empty())));
            assertThrows(NotAllowedException.class, () -> new GetSubRoleUseCase(subroles, permission).getSubRoleById(actor, UUID.randomUUID()));
            assertThrows(NotAllowedException.class, () -> new DeleteSubRoleUseCase(subroles, permission).deleteSubRole(actor, UUID.randomUUID()));
            assertThrows(NotAllowedException.class, () -> new AssignSubRoleToUserUseCase(memberships, subroles, restaurants, users, permission)
                    .assignSubRoleToUser(actor, new MembershipInputDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())));
        }
        verifyNoInteractions(subroles, roles, restaurants, memberships);
    }

    @Test
    void adminCanCreateUpdateReadAndDeleteSubrole() {
        var actor = user("ADMIN").getId();
        var role = new Role(UUID.randomUUID(), "OWNER", "Owner");
        when(roles.getRole(role.getId())).thenReturn(Optional.of(role));
        var created = new CreateSubRoleUseCase(roles, subroles, permission)
                .createSubRole(actor, new CreateSubRoleInputDto("staff", "Staff", role.getId()));
        var existing = new SubRole(created.id(), created.value(), created.label(), role);
        when(subroles.getById(existing.getId())).thenReturn(Optional.of(existing));
        when(subroles.update(any())).thenAnswer(invocation -> invocation.getArgument(0));
        var updated = new UpdateSubRoleUseCase(roles, subroles, permission).update(actor,
                new UpdateSubRoleInputDto(existing.getId(), Optional.empty(), Optional.of("Updated"), Optional.empty()));
        assertEquals("Updated", updated.label());
        assertEquals("staff", updated.value());
        assertEquals(existing.getId(), new GetSubRoleUseCase(subroles, permission).getSubRoleById(actor, existing.getId()).id());
        when(subroles.delete(existing.getId())).thenReturn(true);
        new DeleteSubRoleUseCase(subroles, permission).deleteSubRole(actor, existing.getId());
        verify(subroles).delete(existing.getId());
    }

    @Test
    void assignmentCreatesMembershipAndReusesItsIdOnReassignment() {
        var actor = user("ADMIN").getId();
        var target = user("USER");
        var subrole = new SubRole(UUID.randomUUID(), "manager", "Manager", new Role(UUID.randomUUID(), "OWNER", "Owner"));
        var restaurant = new Restaurant(UUID.randomUUID(), "Restaurant", "Address", "09:00",
                new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of());
        when(restaurants.getRestaurantById(restaurant.getId())).thenReturn(Optional.of(restaurant));
        when(subroles.getById(subrole.getId())).thenReturn(Optional.of(subrole));
        var useCase = new AssignSubRoleToUserUseCase(memberships, subroles, restaurants, users, permission);
        var input = new MembershipInputDto(target.getId(), subrole.getId(), restaurant.getId());
        var created = useCase.assignSubRoleToUser(actor, input);
        assertEquals(target.getId(), created.userId());
        assertEquals(subrole.getId(), created.subRoleId());
        when(memberships.getUserRestaurantMembership(target.getId(), restaurant.getId()))
                .thenReturn(Optional.of(new Membership(created.id(), target, subrole, restaurant)));
        assertEquals(created.id(), useCase.assignSubRoleToUser(actor, input).id());
        verify(memberships, times(2)).save(any());
    }
}
