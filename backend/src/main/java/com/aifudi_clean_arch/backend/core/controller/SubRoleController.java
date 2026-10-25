package com.aifudi_clean_arch.backend.core.controller;

import com.aifudi_clean_arch.backend.core.dto.membership.MembershipInputDto;
import com.aifudi_clean_arch.backend.core.dto.membership.MembershipOutputDto;
import com.aifudi_clean_arch.backend.core.dto.subRole.CreateSubRoleInputDto;
import com.aifudi_clean_arch.backend.core.dto.subRole.SubRoleOutputDto;
import com.aifudi_clean_arch.backend.core.dto.subRole.UpdateSubRoleInputDto;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.*;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class SubRoleController {
    private final CreateSubRoleUseCase createSubRoleUseCase;
    private final UpdateSubRoleUseCase updateSubRoleUseCase;
    private final DeleteSubRoleUseCase deleteSubRoleUseCase;
    private final GetSubRoleUseCase getSubRoleUseCase;
    private final AssignSubRoleToUserUseCase assignSubRoleToUserUseCase;

    public SubRoleOutputDto createSubRole(CreateSubRoleInputDto inputDto) {
        return createSubRoleUseCase.createSubRole(inputDto);
    }

    public SubRoleOutputDto updateSubRole(UpdateSubRoleInputDto inputDto) {
        return updateSubRoleUseCase.update(inputDto);
    }

    public void deleteSubRole(UUID subRoleId) {
        deleteSubRoleUseCase.deleteSubRole(subRoleId);
    }

    public SubRoleOutputDto getSubRole(UUID subRoleId) {
        return getSubRoleUseCase.getSubRoleById(subRoleId);
    }

    public MembershipOutputDto assignSubRoleToUserUseCase(MembershipInputDto inputDto) {
        return assignSubRoleToUserUseCase.assignSubRoleToUser(inputDto);
    }
}
