package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.controller.*;
import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.core.dto.menuItem.*;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.dto.membership.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.infra.security.BasicHeaderAuthenticator;
import com.aifudi_clean_arch.backend.infra.web.json.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.net.URI;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MenuItemApiTest {
    private final MenuItemController controller = mock(MenuItemController.class, withSettings().mockMaker("mock-maker-subclass"));
    private final BasicHeaderAuthenticator authenticator = mock(BasicHeaderAuthenticator.class, withSettings().mockMaker("mock-maker-subclass"));
    private final MenuItemApi api = new MenuItemApi(controller, authenticator);
    private static final String AUTH = "Basic credentials";
    private final UUID userId = UUID.randomUUID();
    private final UUID id = UUID.randomUUID();
    private final UUID referenceId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @BeforeEach
    void authenticate() {
        when(authenticator.authenticate(AUTH)).thenReturn(userId);
    }

    @Test
    void getAuthenticatesAndReturnsControllerResponse() {
        var output = new MenuItemOutputDto(id, "Item", "Descrição", BigDecimal.TEN, true, "item.jpg", referenceId);
        when(controller.getMenuItem(userId, id)).thenReturn(output);

        var response = api.getMenuItem(id, AUTH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).getMenuItem(userId, id);
    }

    @Test
    void createMapsInputAndReturnsControllerResponse() {
        var input = new CreateMenuItemJson("Item", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId);
        var expected = new CreateMenuItemInputDto("Item", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId);
        var output = new MenuItemOutputDto(id, "Item", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId);
        when(controller.createMenuItem(userId, expected)).thenReturn(output);

        var response = api.createMenuItem(AUTH, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).createMenuItem(userId, expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateMapsProvidedAndOmittedFields(boolean supplied) {
        String name = supplied ? "Atualizado" : null;
        String description = supplied ? "Nova descrição" : null;
        BigDecimal price = supplied ? BigDecimal.ONE : null;
        Boolean delivery = supplied ? false : null;
        String picture = supplied ? "new.jpg" : null;
        UUID restaurantId = supplied ? referenceId : null;
        var input = new UpdateMenuItemJson(name, description, price, delivery, picture, restaurantId);
        var expected = new UpdateMenuItemInputDto(id, Optional.ofNullable(name), Optional.ofNullable(description), Optional.ofNullable(price), Optional.ofNullable(delivery), Optional.ofNullable(picture), Optional.ofNullable(restaurantId));
        var output = new MenuItemOutputDto(id, "Atualizado", "Nova descrição", BigDecimal.ONE, false, "new.jpg", referenceId);
        when(controller.updateMenuItem(userId, expected)).thenReturn(output);

        var response = api.updateItem(id, input, AUTH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).updateMenuItem(userId, expected);
    }

    @Test
    void deleteAuthenticatesAndReturnsEmptyResponse() {
        var response = api.deleteMenuItem(id, AUTH);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).deleteMenuItem(userId, id);
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "create", "update", "delete"})
    void invalidCredentialsPreventControllerAccess(String operation) {
        var failure = new InvalidCredentialsException();
        when(authenticator.authenticate(AUTH)).thenThrow(failure);

        var error = assertThrows(InvalidCredentialsException.class, () -> {
            switch (operation) {
                case "get" -> api.getMenuItem(id, AUTH);
                case "create" -> api.createMenuItem(AUTH, new CreateMenuItemJson("Item", "Descrição", BigDecimal.TEN, false, "item.jpg", referenceId));
                case "update" -> api.updateItem(id, new UpdateMenuItemJson(null, null, null, null, null, null), AUTH);
                default -> api.deleteMenuItem(id, AUTH);
            }
        });
        assertSame(failure, error);
        verifyNoInteractions(controller);
    }

    @Test
    void propagatesControllerErrorForExceptionHandler() {
        var failure = new MenuItemNotFountException(id);
        when(controller.getMenuItem(userId, id)).thenThrow(failure);
        assertSame(failure, assertThrows(MenuItemNotFountException.class,
                () -> api.getMenuItem(id, AUTH)));
    }
}
