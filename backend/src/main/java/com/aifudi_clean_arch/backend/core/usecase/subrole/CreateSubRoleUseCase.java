package com.aifudi_clean_arch.backend.core.usecase.subrole;

import com.aifudi_clean_arch.backend.core.domain.SubRole;
import com.aifudi_clean_arch.backend.core.dto.subRole.CreateSubRoleInputDto;
import com.aifudi_clean_arch.backend.core.dto.subRole.SubRoleOutputDto;
import com.aifudi_clean_arch.backend.core.exception.ProtectedSubRoleException;
import com.aifudi_clean_arch.backend.core.exception.RoleNotFoundException;
import com.aifudi_clean_arch.backend.core.gateway.RoleGateway;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CreateSubRoleUseCase {
    private final RoleGateway roleGateway;
    private final SubRoleGateway subRoleGateway;

    public SubRoleOutputDto createSubRole(CreateSubRoleInputDto inputDto) {
        var foundRole = roleGateway.getRole(inputDto.roleId())
                .orElseThrow(() -> new RoleNotFoundException(inputDto.roleId()));

        if("main".equals(inputDto.value())) {
            throw new ProtectedSubRoleException(inputDto.value());
        }
        var newId = UUID.randomUUID();
        var newSubRole = new SubRole(newId, inputDto.value(), inputDto.label(), foundRole);

        subRoleGateway.save(newSubRole);

        return new SubRoleOutputDto(
                newSubRole.getId(),
                newSubRole.getValue(),
                newSubRole.getLabel(),
                newSubRole.getRole().getId()
        );
    }
}
