package com.aifudi_clean_arch.backend.infra.web.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Schema(title = "Update Restaurant Schema")
public record UpdateRestaurantJson(
        @Schema(description = "Name of the restaurant", example = "Aifudi Burger", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 50, message = "The field must be 50 characters or less")
        String name,

        @Schema(description = "Address of the restaurant", example = "Rua das Flores, 100", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "The field must be 255 characters or less")
        String address,

        @Schema(description = "Opening hours of the restaurant", example = "18:00 - 23:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Size(max = 255, message = "The field must be 255 characters or less")
        String openingTime,

        @Schema(description = "Kitchen identifier", example = "00000000-0000-4000-8000-000000000203", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        UUID kitchenId
) {
}
