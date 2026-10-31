package com.aifudi_clean_arch.backend.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MembershipTest {
    private final SubRole subrole = new SubRole(UUID.randomUUID(), "staff", "Equipe", new Role(UUID.randomUUID(), "OWNER", "Dono"));
    private final User user = new User(UUID.randomUUID(), "Ana", "ana@example.com", "ana", "hash", "Endereço", subrole);
    private final Restaurant restaurant = new Restaurant(UUID.randomUUID(), "Restaurante", "Endereço", "11:00",
            new Kitchen(UUID.randomUUID(), "BRAZILIAN", "Brasileira"), Set.of(), Set.of());

    @Test
    void createsMembershipWithoutIdUntilPersistence() {
        var membership = new Membership(null, user, subrole, restaurant);
        assertAll(() -> assertNull(membership.getId()),
                () -> assertSame(user, membership.getUser()),
                () -> assertSame(subrole, membership.getSubrole()),
                () -> assertSame(restaurant, membership.getRestaurant()));
    }

    @Test
    void preservesExistingId() {
        var id = UUID.randomUUID();
        assertEquals(id, new Membership(id, user, subrole, restaurant).getId());
    }

    @Test
    void rejectsMissingUser() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(null, null, subrole, restaurant));
    }

    @Test
    void rejectsMissingSubrole() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(null, user, null, restaurant));
    }

    @Test
    void rejectsMissingRestaurant() {
        assertThrows(IllegalArgumentException.class, () -> new Membership(null, user, subrole, null));
    }
}
