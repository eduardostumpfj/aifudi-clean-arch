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

    public SubRoleOutputDto createSubRole(UUID authenticatedUserId, CreateSubRoleInputDto inputDto) {
        return createSubRoleUseCase.createSubRole(authenticatedUserId, inputDto);
    }

    public SubRoleOutputDto updateSubRole(UUID authenticatedUserId, UpdateSubRoleInputDto inputDto) {
        return updateSubRoleUseCase.update(authenticatedUserId, inputDto);
    }

    public void deleteSubRole(UUID authenticatedUserId, UUID subRoleId) {
        deleteSubRoleUseCase.deleteSubRole(authenticatedUserId, subRoleId);
    }

    public SubRoleOutputDto getSubRole(UUID authenticatedUserId, UUID subRoleId) {
        return getSubRoleUseCase.getSubRoleById(authenticatedUserId, subRoleId);
    }

    public MembershipOutputDto assignSubRoleToUserUseCase(UUID authenticatedUserId, MembershipInputDto inputDto) {
        return assignSubRoleToUserUseCase.assignSubRoleToUser(authenticatedUserId, inputDto);
    }
}
