package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.infra.web.json.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RequestJsonValidationTest {
    @Test
    void acceptsOptionalUpdatesAndExplicitFalse() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validate(new UpdateRestaurantJson(null, null, null, null)).isEmpty());
            assertTrue(validator.validate(new UpdateMenuItemJson(null, null, null, null, null, null)).isEmpty());
            var input = new UpdateMenuItemJson(null, null, null, false, null, null);
            assertTrue(validator.validate(input).isEmpty());
            assertEquals(Boolean.FALSE, input.has_delivery());
        }
    }

    @Test
    void validatesRequiredFieldsAndDatabaseLimits() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var restaurantId = UUID.randomUUID();
            assertTrue(validator.validate(new CreateMenuItemJson("Burger", "Description",
                    new BigDecimal("29.90"), false, "picture.jpg", restaurantId)).isEmpty());
            assertFalse(validator.validate(new CreateMenuItemJson("Burger", "Description",
                    BigDecimal.TEN, null, "picture.jpg", restaurantId)).isEmpty());
            assertFalse(validator.validate(new UpdateMenuItemJson("x".repeat(51), null,
                    null, null, null, null)).isEmpty());
            for (String price : new String[]{"0", "-1", "1.001", "10000000000.00"}) {
                assertFalse(validator.validate(new UpdateMenuItemJson(null, null,
                        new BigDecimal(price), null, null, null)).isEmpty());
            }
            assertFalse(validator.validate(new CreateRestaurantJson(null, "", null, null, null)).isEmpty());
            assertTrue(validator.validate(new CreateRestaurantJson(UUID.randomUUID(), "Restaurant",
                    "Address", "18:00 - 23:00", UUID.randomUUID())).isEmpty());
        }
    }
}
