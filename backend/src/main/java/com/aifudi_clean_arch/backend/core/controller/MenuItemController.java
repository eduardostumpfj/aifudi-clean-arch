package com.aifudi_clean_arch.backend.core.controller;

import com.aifudi_clean_arch.backend.core.dto.menuItem.CreateMenuItemInputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.MenuItemOutputDto;
import com.aifudi_clean_arch.backend.core.dto.menuItem.UpdateMenuItemInputDto;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.CreateMenuItemUseCase;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.DeleteMenuItemUseCase;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.GetMenuItemUseCase;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.UpdateMenuItemUseCase;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class MenuItemController {
    private final CreateMenuItemUseCase createMenuItemUseCase;
    private final DeleteMenuItemUseCase deleteMenuItemUseCase;
    private final UpdateMenuItemUseCase updateMenuItemUseCase;
    private final GetMenuItemUseCase getMenuItemUseCase;

    public MenuItemOutputDto createMenuItem(UUID authId, CreateMenuItemInputDto createMenuItemInputDto) {
        return createMenuItemUseCase.createMenuItem(authId, createMenuItemInputDto);
    }

    public MenuItemOutputDto updateMenuItem(UUID authId, UpdateMenuItemInputDto updateMenuItemInputDto) {
        return updateMenuItemUseCase.updateMenuItem(authId, updateMenuItemInputDto);
    }

    public void deleteMenuItem(UUID authId,UUID menuItemId) {
        deleteMenuItemUseCase.deleteMenuItem(authId, menuItemId);
    }

    public MenuItemOutputDto getMenuItem(UUID authId, UUID menuItemId) {
        return getMenuItemUseCase.getMenuItemById(authId,menuItemId);
    }
}
