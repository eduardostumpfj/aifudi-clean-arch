package com.aifudi_clean_arch.backend.infra.web.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Schema(title = "Create Restaurant Schema")
public record CreateRestaurantJson(
        @Schema(description = "Owner identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID ownerId,

        @Schema(description = "Name of the restaurant", example = "Aifudi Burger", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 50, message = "The field must be 50 characters or less")
        String name,

        @Schema(description = "Address of the restaurant", example = "Rua das Flores, 100", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 255, message = "The field must be 255 characters or less")
        String address,

        @Schema(description = "Opening hours of the restaurant", example = "18:00 - 23:00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 255, message = "The field must be 255 characters or less")
        String openingTime,

        @Schema(description = "Kitchen identifier", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID kitchenId
) {
}
