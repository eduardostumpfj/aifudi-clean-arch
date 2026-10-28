package com.aifudi_clean_arch.backend.infra.web.json;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(title = "Assign Subrole Schema")
public record AssignSubRoleJson(
        @Schema(description = "User receiving the subrole", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID userId,
        @Schema(description = "Subrole to assign", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID subRoleId,
        @Schema(description = "Restaurant of the membership", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull UUID restaurantId
) {}
