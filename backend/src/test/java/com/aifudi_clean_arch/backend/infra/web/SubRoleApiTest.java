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

class SubRoleApiTest {
    private final SubRoleController controller = mock(SubRoleController.class, withSettings().mockMaker("mock-maker-subclass"));
    private final BasicHeaderAuthenticator authenticator = mock(BasicHeaderAuthenticator.class, withSettings().mockMaker("mock-maker-subclass"));
    private final SubRoleApi api = new SubRoleApi(controller, authenticator);
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
        var output = new SubRoleOutputDto(id, "staff", "Equipe", referenceId);
        when(controller.getSubRole(userId, id)).thenReturn(output);

        var response = api.getSubRole(id, AUTH);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).getSubRole(userId, id);
    }

    @Test
    void createMapsInputAndReturnsControllerResponse() {
        var input = new CreateSubRoleJson("staff", "Equipe", referenceId);
        var expected = new CreateSubRoleInputDto("staff", "Equipe", referenceId);
        var output = new SubRoleOutputDto(id, "staff", "Equipe", referenceId);
        when(controller.createSubRole(userId, expected)).thenReturn(output);

        var response = api.createSubRole(AUTH, input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(output, response.getBody());
        assertEquals(URI.create("/v1/subroles/" + id), response.getHeaders().getLocation());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).createSubRole(userId, expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void updateMapsProvidedAndOmittedFields(boolean supplied) {
        String value = supplied ? "manager" : null;
        String label = supplied ? "Gerente" : null;
        UUID roleId = supplied ? referenceId : null;
        var input = new UpdateSubRoleJson(value, label, roleId);
        var expected = new UpdateSubRoleInputDto(id, Optional.ofNullable(value), Optional.ofNullable(label), Optional.ofNullable(roleId));
        var output = new SubRoleOutputDto(id, "manager", "Gerente", referenceId);
        when(controller.updateSubRole(userId, expected)).thenReturn(output);

        var response = api.updateSubRole(id, AUTH, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).updateSubRole(userId, expected);
    }

    @Test
    void deleteAuthenticatesAndReturnsEmptyResponse() {
        var response = api.deleteSubRole(id, AUTH);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).deleteSubRole(userId, id);
    }

    @ParameterizedTest
    @ValueSource(strings = {"get", "create", "update", "delete", "assign"})
    void invalidCredentialsPreventControllerAccess(String operation) {
        var failure = new InvalidCredentialsException();
        when(authenticator.authenticate(AUTH)).thenThrow(failure);

        var error = assertThrows(InvalidCredentialsException.class, () -> {
            switch (operation) {
                case "get" -> api.getSubRole(id, AUTH);
                case "create" -> api.createSubRole(AUTH, new CreateSubRoleJson("staff", "Equipe", referenceId));
                case "update" -> api.updateSubRole(id, AUTH, new UpdateSubRoleJson(null, null, null));
                case "assign" -> api.assignSubRole(AUTH, new AssignSubRoleJson(ownerId, id, referenceId));
                default -> api.deleteSubRole(id, AUTH);
            }
        });
        assertSame(failure, error);
        verifyNoInteractions(controller);
    }

    @Test
    void propagatesControllerErrorForExceptionHandler() {
        var failure = new SubRoleNotFoundException(id);
        when(controller.getSubRole(userId, id)).thenThrow(failure);
        assertSame(failure, assertThrows(SubRoleNotFoundException.class,
                () -> api.getSubRole(id, AUTH)));
    }

    @Test
    void assignmentMapsTargetUserSubroleAndRestaurant() {
        var membershipId = UUID.randomUUID();
        var input = new AssignSubRoleJson(ownerId, id, referenceId);
        var expected = new MembershipInputDto(ownerId, id, referenceId);
        var output = new MembershipOutputDto(membershipId, ownerId, id, referenceId);
        when(controller.assignSubRoleToUserUseCase(userId, expected)).thenReturn(output);

        var response = api.assignSubRole(AUTH, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(output, response.getBody());
        var order = inOrder(authenticator, controller);
        order.verify(authenticator).authenticate(AUTH);
        order.verify(controller).assignSubRoleToUserUseCase(userId, expected);
    }
}
