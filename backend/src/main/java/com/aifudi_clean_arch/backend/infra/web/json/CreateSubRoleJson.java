package com.aifudi_clean_arch.backend.infra.web.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.UUID;

@Schema(title = "CreateSubRoleJson")
public record CreateSubRoleJson(
        @Schema(description = "Subrole value", example = "manager", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 50)
        String value,

        @Schema(description = "Subrole label", example = "Manager", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Size(max = 50)
        String label,

        @Schema(description = "Subrole roleId", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID roleId
) {}
