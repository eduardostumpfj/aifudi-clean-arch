package com.aifudi_clean_arch.backend.infra.config;

import com.aifudi_clean_arch.backend.core.gateway.MembershipGateway;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import com.aifudi_clean_arch.backend.core.gateway.UserGateway;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import org.springframework.context.annotation.Bean;
import com.aifudi_clean_arch.backend.core.usecase.subrole.CheckSubRolePermission;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MembershipDependencyInjectionConfig {

    @Bean
    public AssignSubRoleToUserUseCase assignSubRoleToUserUseCase(
        MembershipGateway membershipGateway,
        SubRoleGateway subRoleGateway,
        RestaurantGateway restaurantGateway,
        UserGateway userGateway,
        CheckSubRolePermission permission
    ) {
        return new AssignSubRoleToUserUseCase(
            membershipGateway, subRoleGateway, restaurantGateway, userGateway, permission
        );
    }
}
