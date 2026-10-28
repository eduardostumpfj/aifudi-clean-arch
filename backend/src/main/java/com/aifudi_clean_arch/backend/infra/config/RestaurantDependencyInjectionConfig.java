package com.aifudi_clean_arch.backend.infra.config;

import com.aifudi_clean_arch.backend.core.controller.RestaurantController;
import com.aifudi_clean_arch.backend.core.usecase.permission.RestaurantPermissionChecker;
import com.aifudi_clean_arch.backend.core.gateway.KitchenGateway;
import com.aifudi_clean_arch.backend.core.gateway.MembershipGateway;
import com.aifudi_clean_arch.backend.core.gateway.RestaurantGateway;
import com.aifudi_clean_arch.backend.core.gateway.SubRoleGateway;
import com.aifudi_clean_arch.backend.core.gateway.TransactionGateway;
import com.aifudi_clean_arch.backend.core.gateway.UserGateway;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.CreateRestaurantUseCase;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.DeleteRestaurantUseCase;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.GetRestaurantUseCase;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.UpdateRestaurantUseCase;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.CheckRestaurantPermission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestaurantDependencyInjectionConfig {

    @Bean
    public CheckRestaurantPermission checkRestaurantPermission(
        RestaurantPermissionChecker permissionChecker
    ) {
        return new CheckRestaurantPermission(permissionChecker);
    }


    @Bean
    public CreateRestaurantUseCase createRestaurantUseCase(
        RestaurantGateway restaurantGateway,
        UserGateway userGateway,
        SubRoleGateway subRoleGateway,
        KitchenGateway kitchenGateway,
        MembershipGateway membershipGateway,
        TransactionGateway transactionGateway,
        CheckRestaurantPermission checkRestaurantPermission
    ) {
        return new CreateRestaurantUseCase(
            restaurantGateway, userGateway, subRoleGateway,
            kitchenGateway, membershipGateway, transactionGateway,checkRestaurantPermission
        );
    }

    @Bean
    public UpdateRestaurantUseCase updateRestaurantUseCase(
        RestaurantGateway restaurantGateway,
        KitchenGateway kitchenGateway,
        CheckRestaurantPermission checkRestaurantPermission
    ) {
        return new UpdateRestaurantUseCase(restaurantGateway, kitchenGateway, checkRestaurantPermission);
    }

    @Bean
    public DeleteRestaurantUseCase deleteRestaurantUseCase(RestaurantGateway restaurantGateway, CheckRestaurantPermission checkRestaurantPermission) {
        return new DeleteRestaurantUseCase(restaurantGateway, checkRestaurantPermission);
    }

    @Bean
    public GetRestaurantUseCase getRestaurantUseCase(RestaurantGateway restaurantGateway, CheckRestaurantPermission checkRestaurantPermission) {
        return new GetRestaurantUseCase(restaurantGateway, checkRestaurantPermission);
    }

    @Bean
    public RestaurantController restaurantController(
        CreateRestaurantUseCase createRestaurantUseCase,
        UpdateRestaurantUseCase updateRestaurantUseCase,
        DeleteRestaurantUseCase deleteRestaurantUseCase,
        GetRestaurantUseCase getRestaurantUseCase
    ) {
        return new RestaurantController(
            createRestaurantUseCase, updateRestaurantUseCase,
            deleteRestaurantUseCase, getRestaurantUseCase
        );
    }
}
