package com.aifudi_clean_arch.backend.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MenuItemTest {
    private final UUID restaurantId = UUID.randomUUID();

    @Test
    void preservesValidDataAndReportsPositivePrice() {
        var id = UUID.randomUUID();
        var price = new java.math.BigDecimal("19.90");
        var item = new MenuItem(id, "Item", "Descrição", price, true, "item.jpg", restaurantId);
        assertAll(() -> assertEquals(id, item.getId()),
                () -> assertEquals("Item", item.getName()),
                () -> assertEquals("Descrição", item.getDescription()),
                () -> assertEquals(price, item.getPrice()),
                () -> assertTrue(item.isHasDelivery()),
                () -> assertEquals("item.jpg", item.getPicture()),
                () -> assertEquals(restaurantId, item.getRestaurantId()),
                () -> assertTrue(item.hasPrice()));
    }

    @Test
    void allowsNewItemWithoutIdAndWithoutDelivery() {
        var item = new MenuItem(null, "Item", "Descrição", java.math.BigDecimal.ONE, false, "item.jpg", restaurantId);
        assertNull(item.getId());
        assertFalse(item.isHasDelivery());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidName(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new MenuItem(null, invalid, "Descrição", java.math.BigDecimal.ONE, true, "item.jpg", restaurantId));
    }

    @Test
    void rejectsMissingRestaurantId() {
        assertThrows(IllegalArgumentException.class, () -> new MenuItem(null, "Item", "Descrição", java.math.BigDecimal.ONE, true, "item.jpg", null));
    }

    @Test
    void rejectsMissingPrice() {
        assertThrows(com.aifudi_clean_arch.backend.core.exception.MenuItemInvalidPrice.class,
                () -> new MenuItem(null, "Item", "Descrição", null, true, "item.jpg", restaurantId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-10"})
    void rejectsNonPositivePrice(String price) {
        assertThrows(com.aifudi_clean_arch.backend.core.exception.MenuItemInvalidPrice.class,
                () -> new MenuItem(null, "Item", "Descrição", new java.math.BigDecimal(price), true, "item.jpg", restaurantId));
    }
}
