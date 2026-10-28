package com.aifudi_clean_arch.backend.infra.config;

import com.aifudi_clean_arch.backend.core.controller.SubRoleController;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CheckSubRolePermission;
import com.aifudi_clean_arch.backend.core.gateway.RoleGateway;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CreateSubRoleUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.DeleteSubRoleUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.GetSubRoleUseCase;
import com.aifudi_clean_arch.backend.core.usecase.subrole.UpdateSubRoleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubRoleDependencyInjectionConfig {
    @Bean
    public CreateSubRoleUseCase createSubRoleUseCase(
        RoleGateway roleGateway,
        SubRoleGateway subRoleGateway,
        CheckSubRolePermission permission
    ) {
        return new CreateSubRoleUseCase(roleGateway, subRoleGateway, permission);
    }

    @Bean
    public UpdateSubRoleUseCase updateSubRoleUseCase(
        RoleGateway roleGateway,
        SubRoleGateway subRoleGateway,
        CheckSubRolePermission permission
    ){
        return new UpdateSubRoleUseCase(roleGateway, subRoleGateway, permission);
    }

    @Bean
    DeleteSubRoleUseCase deleteSubRoleUseCase(
        SubRoleGateway subRoleGateway,
        CheckSubRolePermission permission
    ) {
        return new DeleteSubRoleUseCase(subRoleGateway, permission);
    }

    @Bean
    GetSubRoleUseCase getSubRoleUseCase(
            SubRoleGateway subRoleGateway,
        CheckSubRolePermission permission
    ) {
        return new GetSubRoleUseCase(subRoleGateway, permission);
    }

    @Bean
    public SubRoleController subRoleController(
            CreateSubRoleUseCase createSubRoleUseCase,
            UpdateSubRoleUseCase updateSubRoleUseCase,
            DeleteSubRoleUseCase deleteSubRoleUseCase,
            GetSubRoleUseCase getSubRoleUseCase,
            AssignSubRoleToUserUseCase assignSubRoleToUserUseCase
    ) {
        return new SubRoleController(createSubRoleUseCase, updateSubRoleUseCase, deleteSubRoleUseCase, getSubRoleUseCase, assignSubRoleToUserUseCase);
    }

}
