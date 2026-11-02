package com.aifudi_clean_arch.backend.infra.web.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(title = "Create MenuItem Schema")
public record CreateMenuItemJson(
        @Schema(description = "Name of the menu item", example = "Cheeseburger", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 50, message = "The field must be 50 characters or less")
        String name,

        @Schema(description = "Description of the menu item", example = "Burger with cheese", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 255, message = "The field must be 255 characters or less")
        String description,

        @Schema(description = "Price of the menu item", example = "29.90", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Positive(message = "The price must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "The price must have at most 10 integer digits and 2 decimal places")
        BigDecimal price,

        @Schema(description = "Whether the item is available for delivery", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean has_delivery,

        @Schema(description = "Picture of the menu item", example = "https://example.com/burger.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 255, message = "The field must be 255 characters or less")
        String picture,

        @Schema(description = "Restaurant identifier", example = "00000000-0000-4000-8000-000000000301", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID restaurantId
) {
}
