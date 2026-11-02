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

        @Schema(description = "Subrole roleId", example = "00000000-0000-4000-8000-000000000002", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID roleId
) {}
