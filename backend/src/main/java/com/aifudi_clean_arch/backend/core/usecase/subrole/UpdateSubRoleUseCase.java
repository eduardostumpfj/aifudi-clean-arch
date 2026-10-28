package com.aifudi_clean_arch.backend.core.usecase.subrole;

import com.aifudi_clean_arch.backend.core.domain.SubRole;
import com.aifudi_clean_arch.backend.core.dto.subRole.UpdateSubRoleInputDto;
import com.aifudi_clean_arch.backend.core.dto.subRole.SubRoleOutputDto;
import com.aifudi_clean_arch.backend.core.exception.ProtectedSubRoleException;
import com.aifudi_clean_arch.backend.core.exception.RoleNotFoundException;
import com.aifudi_clean_arch.backend.core.exception.SubRoleNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.RoleGateway;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import lombok.AllArgsConstructor;
import java.util.UUID;

@AllArgsConstructor
public class UpdateSubRoleUseCase {
    private final RoleGateway roleGateway;
    private final SubRoleGateway subRoleGateway;
    private final CheckSubRolePermission permission;

    public SubRoleOutputDto update(UUID authenticatedUserId, UpdateSubRoleInputDto inputDto){
        permission.checkUpdateSubRole(authenticatedUserId);
        var current = subRoleGateway.getById(inputDto.id())
                .orElseThrow(() ->
                        new SubRoleNotFoundException(inputDto.id()));

        if(current.isMainOwner()){
            throw new ProtectedSubRoleException(inputDto.id());
        }

        var role = inputDto.roleId()
                .map(roleId -> roleGateway.getRole(roleId)
                        .orElseThrow(() ->
                                new RoleNotFoundException(roleId)))
                .orElseGet(current::getRole);


        var updated = new SubRole(
                current.getId(),
                inputDto.value().orElse(current.getValue()),
                inputDto.label().orElse(current.getLabel()),
                role
        );

        if ("main".equals(updated.getValue())) {
            throw new ProtectedSubRoleException(updated.getId());
        }

        var updatedSubRole = subRoleGateway.update(updated);

        return new SubRoleOutputDto(
                updatedSubRole.getId(),
                updatedSubRole.getValue(),
                updatedSubRole.getLabel(),
                updatedSubRole.getRole().getId()
        );
    }
}
