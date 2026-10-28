package com.aifudi_clean_arch.backend.core.usecase.subrole;

import com.aifudi_clean_arch.backend.core.dto.subRole.SubRoleOutputDto;
import com.aifudi_clean_arch.backend.core.exception.SubRoleNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class GetSubRoleUseCase {
    private final SubRoleGateway subRoleGateway;
    private final CheckSubRolePermission permission;

    public SubRoleOutputDto getSubRoleById(UUID authenticatedUserId, UUID id) {
        permission.checkGetSubRole(authenticatedUserId);
        var subRole = subRoleGateway.getById(id)
                .orElseThrow(() -> new SubRoleNotFoundException(id));

        return new SubRoleOutputDto(
                subRole.getId(),
                subRole.getValue(),
                subRole.getLabel(),
                subRole.getRole().getId()
        );
    }

}
