package com.aifudi_clean_arch.backend.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {
    private final UUID id = UUID.randomUUID();
    private final Kitchen kitchen = new Kitchen(UUID.randomUUID(), "BRAZILIAN", "Brasileira");

    @Test
    void preservesValidDataAndAllowsIdToBeGeneratedOnSave() {
        var restaurant = new Restaurant(null, "Restaurante", "Endereço", "11:00 - 22:00", kitchen, Set.of(), Set.of());
        assertAll(() -> assertNull(restaurant.getId()),
                () -> assertEquals("Restaurante", restaurant.getName()),
                () -> assertEquals("Endereço", restaurant.getAddress()),
                () -> assertEquals("11:00 - 22:00", restaurant.getOpeningTime()),
                () -> assertSame(kitchen, restaurant.getKitchen()),
                () -> assertTrue(restaurant.getMembers().isEmpty()),
                () -> assertTrue(restaurant.getMenuItems().isEmpty()));
    }

    @Test
    void preservesExistingIdAndAssociations() {
        var subrole = new SubRole(UUID.randomUUID(), "staff", "Equipe", new Role(UUID.randomUUID(), "OWNER", "Dono"));
        var user = new User(UUID.randomUUID(), "Ana", "ana@example.com", "ana", "hash", "Endereço", subrole);
        var initial = new Restaurant(id, "Restaurante", "Endereço", "11:00", kitchen, Set.of(), Set.of());
        var member = new Membership(UUID.randomUUID(), user, subrole, initial);
        var item = new MenuItem(UUID.randomUUID(), "Item", "Descrição", java.math.BigDecimal.TEN, false, "item.jpg", id);
        var restaurant = new Restaurant(id, "Restaurante", "Endereço", "11:00", kitchen, Set.of(member), Set.of(item));
        assertAll(() -> assertEquals(id, restaurant.getId()),
                () -> assertEquals(Set.of(member), restaurant.getMembers()),
                () -> assertEquals(Set.of(item), restaurant.getMenuItems()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidName(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, invalid, "Endereço", "11:00", kitchen, Set.of(), Set.of()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidAddress(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, "Restaurante", invalid, "11:00", kitchen, Set.of(), Set.of()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidOpeningTime(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, "Restaurante", "Endereço", invalid, kitchen, Set.of(), Set.of()));
    }

    @Test
    void rejectsMissingKitchen() {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, "Restaurante", "Endereço", "11:00", null, Set.of(), Set.of()));
    }

    @Test
    void rejectsMissingMembers() {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, "Restaurante", "Endereço", "11:00", kitchen, null, Set.of()));
    }

    @Test
    void rejectsMissingMenuItems() {
        assertThrows(IllegalArgumentException.class, () -> new Restaurant(id, "Restaurante", "Endereço", "11:00", kitchen, Set.of(), null));
    }
}
