package com.aifudi_clean_arch.backend.infra.config;

import com.aifudi_clean_arch.backend.core.controller.RestaurantController;
import com.aifudi_clean_arch.backend.core.gateway.RoleGateway;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CreateSubRoleUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DependencyInjectionConfig {
//    @Bean
//    public CreateSubRoleUseCase createSubRoleUseCase(
//        RoleGateway roleGateway,
//        SubRoleGateway subRoleGateway
//    ) {
//        return new CreateSubRoleUseCase(roleGateway, subRoleGateway);
//    }

}
