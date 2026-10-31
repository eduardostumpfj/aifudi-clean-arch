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

class RestaurantApiTest {
    private final RestaurantController controller = mock(RestaurantController.class, withSettings().mockMaker("mock-maker-subclass"));
    private final BasicHeaderAuthenticator authenticator = mock(BasicHeaderAuthenticator.class, withSettings().mockMaker("mock-maker-subclass"));
    private final RestaurantApi api = new RestaurantApi(controller, authenticator);
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
        var output = new GetRestaurantOutputDto(id, "Restaurante", "Endereço", "11:00", referenceId, Set.of(), Set.of());
        when(controller.getRestaurant(userId, id)).thenReturn(output);

        var response = api.getRestaurant(id, AUTH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).getRestaurant(userId, id);
    }

    @Test
    void createMapsInputAndReturnsControllerResponse() {
        var input = new CreateRestaurantJson(ownerId, "Restaurante", "Endereço", "11:00", referenceId);
        var expected = new CreateRestaurantInputDto(ownerId, "Restaurante", "Endereço", "11:00", referenceId);
        var output = new CreateRestaurantOutputDto(id, "Restaurante", "Endereço", "11:00", referenceId, ownerId);
        when(controller.createRestaurant(userId, expected)).thenReturn(output);

        var response = api.createRestaurant(AUTH, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).createRestaurant(userId, expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateMapsProvidedAndOmittedFields(boolean supplied) {
        String name = supplied ? "Atualizado" : null;
        String address = supplied ? "Endereço novo" : null;
        String time = supplied ? "18:00" : null;
        UUID kitchenId = supplied ? referenceId : null;
        var input = new UpdateRestaurantJson(name, address, time, kitchenId);
        var expected = new UpdateRestaurantInputDto(id, Optional.ofNullable(name), Optional.ofNullable(address), Optional.ofNullable(time), Optional.ofNullable(kitchenId));
        var output = new UpdateRestaurantOutputDto(id, "Atualizado", "Endereço novo", "18:00", referenceId);
        when(controller.updateRestaurant(userId, expected)).thenReturn(output);

        var response = api.updateRestaurant(id, AUTH, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).updateRestaurant(userId, expected);
    }

    @Test
    void deleteAuthenticatesAndReturnsEmptyResponse() {
        var response = api.deleteRestaurant(id, AUTH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).deleteRestaurant(userId, id);
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "create", "update", "delete"})
    void invalidCredentialsPreventControllerAccess(String operation) {
        var failure = new InvalidCredentialsException();
        when(authenticator.authenticate(AUTH)).thenThrow(failure);

        var error = assertThrows(InvalidCredentialsException.class, () -> {
            switch (operation) {
                case "get" -> api.getRestaurant(id, AUTH);
                case "create" -> api.createRestaurant(AUTH, new CreateRestaurantJson(ownerId, "Restaurante", "Endereço", "11:00", referenceId));
                case "update" -> api.updateRestaurant(id, AUTH, new UpdateRestaurantJson(null, null, null, null));
                default -> api.deleteRestaurant(id, AUTH);
            }
        });
        assertSame(failure, error);
        verifyNoInteractions(controller);
    }

    @Test
    void propagatesControllerErrorForExceptionHandler() {
        var failure = new RestaurantNotFoundException(id);
        when(controller.getRestaurant(userId, id)).thenThrow(failure);
        assertSame(failure, assertThrows(RestaurantNotFoundException.class,
                () -> api.getRestaurant(id, AUTH)));
    }
}
