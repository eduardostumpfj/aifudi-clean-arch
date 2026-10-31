package com.aifudi_clean_arch.backend.core.usecase.permission;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.usecase.permission.RestaurantPermissionChecker;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.core.dto.restaurant.UpdateRestaurantInputDto;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.core.gateway.*;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantPermissionTest {
    private final UserGateway users = mock(UserGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MembershipGateway memberships = mock(MembershipGateway.class, withSettings().mockMaker("mock-maker-subclass"));
    private final PasswordEncoder passwords = mock(PasswordEncoder.class, withSettings().mockMaker("mock-maker-subclass"));
    private final CheckRestaurantPermission permission = new CheckRestaurantPermission(new RestaurantPermissionChecker(users, memberships));
    private final BasicHeaderAuthenticator authenticator = new BasicHeaderAuthenticator(users, passwords);
    private final UUID restaurantId = UUID.randomUUID();
    private final SubRole owner = new SubRole(UUID.randomUUID(), "mainOwner", "Owner", new Role(UUID.randomUUID(), "OWNER", "Owner"));
    private final User user = new User(UUID.randomUUID(), "User", "u@example.com", "user", "hash", "Address", owner);

    private String auth(String credentials) {
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private void authenticate() {
        when(users.getUserByLogin("user")).thenReturn(Optional.of(user));
        when(users.getUserById(user.getId())).thenReturn(Optional.of(user));
        when(passwords.matches("secret", "hash")).thenReturn(true);
    }

    @Test
    void rejectsMalformedHeaders() {
        for (String header : Arrays.asList(null, "", "Basic", "Bearer abc", "Basic %%%")) {
            assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(header));
        }
        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(null));
        for (String credentials : List.of("user", ":secret", "user:")) {
            assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(auth(credentials)));
        }
        verifyNoInteractions(users, passwords, memberships);
    }

    @Test
    void rejectsUnknownUserAndWrongPassword() {
        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(auth("user:secret")));
        when(users.getUserByLogin("user")).thenReturn(Optional.of(user));
        when(users.getUserById(user.getId())).thenReturn(Optional.of(user));
        assertThrows(InvalidCredentialsException.class, () -> authenticator.authenticate(auth("user:wrong")));
        verifyNoInteractions(memberships);
    }

    @Test
    void allowsOwnerAndAdminButRejectsOtherRoles() {
        authenticate();
        var restaurant = new Restaurant(restaurantId, "Restaurant", "Address", "09:00",
                new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of());
        for (String role : List.of("OWNER", "ADMIN", "CLIENT")) {
            var subrole = new SubRole(UUID.randomUUID(), "member", "Member", new Role(UUID.randomUUID(), role, role));
            when(memberships.getUserRestaurantMembership(user.getId(), restaurantId))
                    .thenReturn(Optional.of(new Membership(UUID.randomUUID(), user, subrole, restaurant)));
            if (role.equals("CLIENT")) {
                assertThrows(NotAllowedException.class, () -> permission.checkUpdateRestaurant(user.getId(), restaurantId));
            } else {
                assertDoesNotThrow(() -> permission.checkUpdateRestaurant(user.getId(), restaurantId));
            }
        }
    }

    @Test
    void membershipInAnotherRestaurantDoesNotAuthorizeUpdate() {
        authenticate();
        var other = new Restaurant(UUID.randomUUID(), "Other", "Address", "09:00",
                new Kitchen(UUID.randomUUID(), "type", "Type"), Set.of(), Set.of());
        when(memberships.getUserRestaurantMembership(user.getId(), other.getId()))
                .thenReturn(Optional.of(new Membership(UUID.randomUUID(), user, owner, other)));
        var restaurants = mock(RestaurantGateway.class, withSettings().mockMaker("mock-maker-subclass"));
        var kitchens = mock(KitchenGateway.class, withSettings().mockMaker("mock-maker-subclass"));
        var update = new UpdateRestaurantUseCase(restaurants, kitchens, permission);
        var input = new UpdateRestaurantInputDto(restaurantId, Optional.of("Changed"), Optional.empty(), Optional.empty(), Optional.empty());
        assertThrows(NotAllowedException.class, () -> update.updateRestaurant(authenticator.authenticate(auth("user:secret")), input));
        verify(memberships).getUserRestaurantMembership(user.getId(), restaurantId);
        verifyNoInteractions(restaurants, kitchens);
    }
}
