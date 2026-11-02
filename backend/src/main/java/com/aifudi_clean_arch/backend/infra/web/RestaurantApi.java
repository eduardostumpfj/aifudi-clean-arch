package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.RestaurantController;
import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.infra.web.json.UpdateRestaurantJson;
import com.aifudi_clean_arch.backend.infra.web.json.CreateRestaurantJson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Restaurantes", description = "Cadastro e gestão de restaurantes.")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Operação não permitida",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
})
@RestController
@RequestMapping("v1/restaurants")
@RequiredArgsConstructor
public class RestaurantApi {
    private final RestaurantController restaurantController;
    private final BasicHeaderAuthenticator authenticator;

    @PostMapping("/register")
    @Operation(summary = "Cadastrar restaurante", description = "Cria o restaurante e o vínculo mainOwner do proprietário. Requer ADMIN ou OWNER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante cadastrado", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Corpo ou parâmetros inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<CreateRestaurantOutputDto> createRestaurant(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateRestaurantJson input
    ) {
        var userId = authenticator.authenticate(authHeader);
        var created = restaurantController.createRestaurant(userId, new CreateRestaurantInputDto(
                input.ownerId(), input.name(), input.address(), input.openingTime(), input.kitchenId()));
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar restaurante", description = "Atualiza os campos informados. Requer ADMIN ou vínculo de proprietário no restaurante.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante atualizado", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Corpo ou parâmetros inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UpdateRestaurantOutputDto> updateRestaurant(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody UpdateRestaurantJson input
    ){
        var userId = authenticator.authenticate(authHeader);
        var updated = restaurantController.updateRestaurant(userId, new UpdateRestaurantInputDto(
                id, Optional.ofNullable(input.name()), Optional.ofNullable(input.address()),
                Optional.ofNullable(input.openingTime()), Optional.ofNullable(input.kitchenId())));
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar restaurante", description = "Retorna o restaurante e os IDs dos seus vínculos e itens do cardápio.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante encontrado", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<GetRestaurantOutputDto> getRestaurant(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        var userId = authenticator.authenticate(authHeader);
        var restaurant = restaurantController.getRestaurant(userId, id);
        return ResponseEntity.ok(restaurant);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir restaurante", description = "Requer ADMIN ou vínculo de proprietário. Dependências existentes impedem a exclusão.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Restaurante excluído", content = @Content),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> deleteRestaurant(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        var userId = authenticator.authenticate(authHeader);
        restaurantController.deleteRestaurant(userId, id);

        return ResponseEntity.ok().build();
    }

}
