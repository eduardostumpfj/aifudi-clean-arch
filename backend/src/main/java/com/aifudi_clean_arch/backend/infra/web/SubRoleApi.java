package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.SubRoleController;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.dto.membership.*;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.infra.web.json.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("v1/subroles")
@RequiredArgsConstructor
public class SubRoleApi {
    private final SubRoleController subRoleController;
    private final BasicHeaderAuthenticator authenticator;

    @PostMapping
    public ResponseEntity<SubRoleOutputDto> createSubRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateSubRoleJson input) {
        var userId = authenticator.authenticate(authHeader);
        var created = subRoleController.createSubRole(userId,
                new CreateSubRoleInputDto(input.value(), input.label(), input.roleId()));
        return ResponseEntity.created(URI.create("/v1/subroles/" + created.id())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubRoleOutputDto> getSubRole(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(subRoleController.getSubRole(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubRoleOutputDto> updateSubRole(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody UpdateSubRoleJson input) {
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(subRoleController.updateSubRole(userId, new UpdateSubRoleInputDto(
                id, Optional.ofNullable(input.value()), Optional.ofNullable(input.label()),
                Optional.ofNullable(input.roleId()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubRole(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        var userId = authenticator.authenticate(authHeader);
        subRoleController.deleteSubRole(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public ResponseEntity<MembershipOutputDto> assignSubRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody AssignSubRoleJson input) {
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(subRoleController.assignSubRoleToUserUseCase(userId,
                new MembershipInputDto(input.userId(), input.subRoleId(), input.restaurantId())));
    }
}
