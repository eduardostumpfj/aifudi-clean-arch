package com.aifudi_clean_arch.backend.core.usecase.membership;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.dto.membership.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CheckSubRolePermission;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssignSubRoleToUserUseCaseTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final MembershipGateway memberships = mockType(MembershipGateway.class);
    private final SubRoleGateway subroles = mockType(SubRoleGateway.class);
    private final RestaurantGateway restaurants = mockType(RestaurantGateway.class);
    private final UserGateway users = mockType(UserGateway.class);
    private final CheckSubRolePermission permission = mockType(CheckSubRolePermission.class);
    private final AssignSubRoleToUserUseCase useCase = new AssignSubRoleToUserUseCase(memberships, subroles, restaurants, users, permission);
    private final UUID actorId = UUID.randomUUID();
    private final Role ownerRole = new Role(UUID.randomUUID(), "OWNER", "Proprietário");
    private final SubRole staff = new SubRole(UUID.randomUUID(), "staff", "Equipe", ownerRole);
    private final User target = new User(UUID.randomUUID(), "Usuário", "user@example.com", "user", "hash", "Endereço", staff);
    private final Restaurant restaurant = new Restaurant(UUID.randomUUID(), "Restaurante", "Endereço", "11:00 - 22:00",
            new Kitchen(UUID.randomUUID(), "BRAZILIAN", "Brasileira"), Set.of(), Set.of());

    private MembershipInputDto input(SubRole subrole) {
        return new MembershipInputDto(target.getId(), subrole.getId(), restaurant.getId());
    }
    private void resourcesExist(SubRole subrole) {
        when(users.getUserById(target.getId())).thenReturn(Optional.of(target));
        when(restaurants.getRestaurantById(restaurant.getId())).thenReturn(Optional.of(restaurant));
        when(subroles.getById(subrole.getId())).thenReturn(Optional.of(subrole));
    }

    @Test
    void createsMembershipWithoutIdAndReturnsPersistedData() {
        resourcesExist(staff);
        when(memberships.getUserRestaurantMembership(target.getId(), restaurant.getId())).thenReturn(Optional.empty());
        var generatedId = UUID.randomUUID();
        when(memberships.save(any())).thenReturn(new Membership(generatedId, target, staff, restaurant));

        var result = useCase.assignSubRoleToUser(actorId, input(staff));

        assertEquals(new MembershipOutputDto(generatedId, target.getId(), staff.getId(), restaurant.getId()), result);
        var captor = ArgumentCaptor.forClass(Membership.class);
        var order = inOrder(permission, users, restaurants, subroles, memberships);
        order.verify(permission).checkAssignSubRole(actorId);
        order.verify(users).getUserById(target.getId());
        order.verify(restaurants).getRestaurantById(restaurant.getId());
        order.verify(subroles).getById(staff.getId());
        order.verify(memberships).getUserRestaurantMembership(target.getId(), restaurant.getId());
        order.verify(memberships).save(captor.capture());
        var pending = captor.getValue();
        assertAll(() -> assertNull(pending.getId()),
                () -> assertSame(target, pending.getUser()),
                () -> assertSame(staff, pending.getSubrole()),
                () -> assertSame(restaurant, pending.getRestaurant()));
    }

    @Test
    void changesExistingSubroleWhilePreservingMembershipId() {
        var replacement = new SubRole(UUID.randomUUID(), "manager", "Gerente", ownerRole);
        resourcesExist(replacement);
        var existing = new Membership(UUID.randomUUID(), target, staff, restaurant);
        when(memberships.getUserRestaurantMembership(target.getId(), restaurant.getId())).thenReturn(Optional.of(existing));
        when(memberships.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = useCase.assignSubRoleToUser(actorId, input(replacement));

        assertEquals(new MembershipOutputDto(existing.getId(), target.getId(), replacement.getId(), restaurant.getId()), result);
        var captor = ArgumentCaptor.forClass(Membership.class);
        verify(memberships).save(captor.capture());
        var updated = captor.getValue();
        assertAll(() -> assertEquals(existing.getId(), updated.getId()),
                () -> assertSame(replacement, updated.getSubrole()),
                () -> assertSame(target, updated.getUser()),
                () -> assertSame(restaurant, updated.getRestaurant()));
        assertSame(staff, target.getClientSubrole());
    }

    @Test
    void rejectsUnauthorizedActorBeforeAccessingGateways() {
        doThrow(new NotAllowedException()).when(permission).checkAssignSubRole(actorId);
        assertThrows(NotAllowedException.class, () -> useCase.assignSubRoleToUser(actorId, input(staff)));
        verifyNoInteractions(users, restaurants, subroles, memberships);
    }

    @Test
    void rejectsMissingUser() {
        when(users.getUserById(target.getId())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> useCase.assignSubRoleToUser(actorId, input(staff)));
        verifyNoInteractions(restaurants, subroles, memberships);
    }

    @Test
    void rejectsMissingRestaurant() {
        when(users.getUserById(target.getId())).thenReturn(Optional.of(target));
        when(restaurants.getRestaurantById(restaurant.getId())).thenReturn(Optional.empty());
        assertThrows(RestaurantNotFoundException.class, () -> useCase.assignSubRoleToUser(actorId, input(staff)));
        verifyNoInteractions(subroles, memberships);
    }

    @Test
    void rejectsMissingSubrole() {
        resourcesExist(staff);
        when(subroles.getById(staff.getId())).thenReturn(Optional.empty());
        assertThrows(SubRoleNotFoundException.class, () -> useCase.assignSubRoleToUser(actorId, input(staff)));
        verifyNoInteractions(memberships);
    }

    @Test
    void rejectsAssignmentOfMainOwner() {
        var main = new SubRole(UUID.randomUUID(), "mainOwner", "Principal", ownerRole);
        resourcesExist(main);
        assertThrows(ProtectedSubRoleException.class, () -> useCase.assignSubRoleToUser(actorId, input(main)));
        verify(memberships, never()).save(any());
    }

    @Test
    void rejectsReplacingExistingMainOwner() {
        resourcesExist(staff);
        var main = new SubRole(UUID.randomUUID(), "mainOwner", "Principal", ownerRole);
        when(memberships.getUserRestaurantMembership(target.getId(), restaurant.getId()))
                .thenReturn(Optional.of(new Membership(UUID.randomUUID(), target, main, restaurant)));
        assertThrows(ProtectedSubRoleException.class, () -> useCase.assignSubRoleToUser(actorId, input(staff)));
        verify(memberships, never()).save(any());
    }
}
