package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.SubRoleController;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.dto.membership.*;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.infra.web.json.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Tipos de Usuário", description = "Gestão de tipos de usuários exclusiva de ADMIN. Atribuição de vínculos por ADMIN ou mainOwner do restaurante.")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Credenciais ausentes ou inválidas",
                    content = @Content),
        @ApiResponse(responseCode = "403", description = "Operação não permitida",
                    content = @Content),
        @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content)
})
@RestController
@RequestMapping("v1/subroles")
@RequiredArgsConstructor
public class SubRoleApi {
    private final SubRoleController subRoleController;
    private final BasicHeaderAuthenticator authenticator;

    @PostMapping
    @Operation(summary = "Cadastrar subrole", description = "Cria um subrole associado a um papel. O valor mainOwner é reservado.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Subrole cadastrado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Corpo ou parâmetros inválidos",
                    content = @Content),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content)
    })
    public ResponseEntity<SubRoleOutputDto> createSubRole(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateSubRoleJson input) {
        var userId = authenticator.authenticate(authHeader);
        var created = subRoleController.createSubRole(userId,
                new CreateSubRoleInputDto(input.value(), input.label(), input.roleId()));
        return ResponseEntity.created(URI.create("/v1/subroles/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar subrole", description = "Consulta um subrole pelo seu ID. Requer ADMIN.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subrole encontrado", content = @Content),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content)
    })
    public ResponseEntity<SubRoleOutputDto> getSubRole(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(subRoleController.getSubRole(userId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar subrole", description = "Atualiza apenas os campos informados. O proprietário principal é protegido.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subrole atualizado", content = @Content),
        @ApiResponse(responseCode = "400", description = "Corpo ou parâmetros inválidos",
                    content = @Content),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content)
    })
    public ResponseEntity<SubRoleOutputDto> updateSubRole(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody UpdateSubRoleJson input) {
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(subRoleController.updateSubRole(userId, new UpdateSubRoleInputDto(
                id, Optional.ofNullable(input.value()), Optional.ofNullable(input.label()),
                Optional.ofNullable(input.roleId()))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir subrole", description = "Subroles protegidos ou ainda referenciados não podem ser excluídos.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Subrole excluído", content = @Content),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteSubRole(
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        var userId = authenticator.authenticate(authHeader);
        subRoleController.deleteSubRole(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    @Operation(summary = "Atribuir subrole a usuário", description = "Cria ou atualiza um membership sem alterar o perfil global. ADMIN pode atribuir qualquer subrole. mainOwner atua apenas no próprio restaurante e não pode atribuir ou substituir ADMIN e mainOwner.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Subrole atribuído", content = @Content),
        @ApiResponse(responseCode = "400", description = "Corpo ou parâmetros inválidos",
                    content = @Content),
        @ApiResponse(responseCode = "404", description = "Recurso não encontrado",
                    content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflito com dados existentes ou recursos relacionados",
                    content = @Content)
    })
    public ResponseEntity<MembershipOutputDto> assignSubRole(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AssignSubRoleJson input) {
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(subRoleController.assignSubRoleToUserUseCase(userId,
                new MembershipInputDto(input.userId(), input.subRoleId(), input.restaurantId())));
    }
}
