package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.MenuItemController;
import com.aifudi_clean_arch.backend.core.dto.menuItem.CreateMenuItemInputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.MenuItemOutputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.UpdateMenuItemInputDto;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.infra.web.json.CreateMenuItemJson;
import com.aifudi_clean_arch.backend.infra.web.json.UpdateMenuItemJson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v1/menuitems")
@RequiredArgsConstructor
public class MenuItemApi {
    private final MenuItemController menuItemController;
    private final BasicHeaderAuthenticator authenticator;

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemOutputDto> getMenuItem(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        log.info("GET MENUITEM -> {}", id);
        var userId = authenticator.authenticate(authHeader);
        return ResponseEntity.ok(menuItemController.getMenuItem(userId,id));
    }

    @PostMapping
    public ResponseEntity<MenuItemOutputDto> createMenuItem(
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
    public ResponseEntity<MenuItemOutputDto> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMenuItemJson updateItem,
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
    public ResponseEntity<MenuItemOutputDto> deleteMenuItem(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){
        log.info("DELETE MENUITEM -> {}", id);
        var userId = authenticator.authenticate(authHeader);
        menuItemController.deleteMenuItem(userId, id);

        return ResponseEntity.status(204).build();
    }
}
