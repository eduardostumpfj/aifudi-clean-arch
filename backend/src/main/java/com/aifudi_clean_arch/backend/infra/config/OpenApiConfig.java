package com.aifudi_clean_arch.backend.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI aifudiOpenApi() {
        return new OpenAPI()
                .info(new Info().title("Aifudi Clean Arch API")
                        .description("API do Tech Challenge: restaurantes, cardápio e subroles. "
                                + "Use Authorize para informar o login e a senha de um usuário cadastrado.")
                        .version("v0.0.1"))
                .components(new Components().addSecuritySchemes("basicAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"));
    }
}
