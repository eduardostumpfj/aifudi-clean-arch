package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.MenuItemController;
import com.aifudi_clean_arch.backend.core.dto.menuItem.CreateMenuItemInputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.MenuItemOutputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.UpdateMenuItemInputDto;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.infra.web.json.CreateMenuItemJson;
import com.aifudi_clean_arch.backend.infra.web.json.UpdateMenuItemJson;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Tag(name = "Cardápio", description = "Cadastro e gestão dos itens de cardápio.")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "403", description = "Operação não permitida",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
})
@RestController
@RequestMapping("/v1/menuitems")
@RequiredArgsConstructor
public class MenuItemApi {
    private final MenuItemController menuItemController;
    private final BasicHeaderAuthenticator authenticator;

    @GetMapping("/{id}")
    @Operation(summary = "Consultar item do cardápio", description = "Disponível para usuários autenticados com papel ADMIN, OWNER ou USER.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item encontrado", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MenuItemOutputDto> getMenuItem(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        log.info("GET MENUITEM -> {}", id);
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(menuItemController.getMenuItem(userId,id));
    }

    @PostMapping
    @Operation(summary = "Cadastrar item do cardápio", description = "Requer ADMIN ou vínculo de proprietário no restaurante informado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item cadastrado", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Corpo ou parâmetros inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MenuItemOutputDto> createMenuItem(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid
            @RequestBody CreateMenuItemJson postItem

    ){
        log.info("POST MENUITEM");
        var userId = authenticator.authenticate(authHeader);
        var created = menuItemController.createMenuItem(userId, new CreateMenuItemInputDto(
                postItem.name(),
                postItem.description(),
                postItem.price(),
                postItem.has_delivery(),
                postItem.picture(),
                postItem.restaurantId()
        ));

        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar item do cardápio", description = "Atualiza apenas os campos informados. Requer permissão para gerenciar o item.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item atualizado", useReturnTypeSchema = true),
        @ApiResponse(responseCode = "400", description = "Corpo ou parâmetros inválidos",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MenuItemOutputDto> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMenuItemJson updateItem,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        log.info("UPDATE MENUITEM -> {}", id);
        var userId = authenticator.authenticate(authHeader);
        var updated = menuItemController.updateMenuItem(userId, new UpdateMenuItemInputDto(
                id,
                Optional.ofNullable(updateItem.name()),
                Optional.ofNullable(updateItem.description()),
                Optional.ofNullable(updateItem.price()),
                Optional.ofNullable(updateItem.has_delivery()),
                Optional.ofNullable(updateItem.picture()),
                Optional.ofNullable(updateItem.restaurantId())
        ));

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item do cardápio", description = "Requer ADMIN ou vínculo de proprietário no restaurante do item.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item excluído", content = @Content),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<MenuItemOutputDto> deleteMenuItem(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        log.info("DELETE MENUITEM -> {}", id);
        var userId = authenticator.authenticate(authHeader);
        menuItemController.deleteMenuItem(userId, id);

        return ResponseEntity.status(204).build();
    }
}
