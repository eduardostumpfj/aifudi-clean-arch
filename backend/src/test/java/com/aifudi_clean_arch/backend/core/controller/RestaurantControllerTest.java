package com.aifudi_clean_arch.backend.core.controller;

import com.aifudi_clean_arch.backend.core.dto.restaurant.*;
import com.aifudi_clean_arch.backend.core.dto.menuItem.*;
import com.aifudi_clean_arch.backend.core.dto.subRole.*;
import com.aifudi_clean_arch.backend.core.dto.membership.*;
import com.aifudi_clean_arch.backend.core.exception.NotAllowedException;
import com.aifudi_clean_arch.backend.core.usecase.restaurant.*;
import com.aifudi_clean_arch.backend.core.usecase.menuItem.*;
import com.aifudi_clean_arch.backend.core.usecase.subrole.*;
import com.aifudi_clean_arch.backend.core.usecase.membership.AssignSubRoleToUserUseCase;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RestaurantControllerTest {
    private <T> T mockType(Class<T> type) {
        return mock(type, withSettings().mockMaker("mock-maker-subclass"));
    }
    private final CreateRestaurantUseCase create = mockType(CreateRestaurantUseCase.class);
    private final UpdateRestaurantUseCase update = mockType(UpdateRestaurantUseCase.class);
    private final DeleteRestaurantUseCase delete = mockType(DeleteRestaurantUseCase.class);
    private final GetRestaurantUseCase get = mockType(GetRestaurantUseCase.class);
    private final RestaurantController controller = new RestaurantController(create, update, delete, get);
    private final UUID userId = UUID.randomUUID();
    private final UUID id = UUID.randomUUID();
    private final UUID referenceId = UUID.randomUUID();
    private final UUID ownerId = UUID.randomUUID();

    @Test
    void delegatesCreateWithAuthenticatedUserAndOriginalInput() {
        var input = new CreateRestaurantInputDto(ownerId, "Restaurante", "Endereço", "11:00", referenceId);
        var output = new CreateRestaurantOutputDto(id, "Restaurante", "Endereço", "11:00", referenceId, ownerId);
        when(create.createRestaurant(userId, input)).thenReturn(output);

        var result = controller.createRestaurant(userId, input);

        assertSame(output, result);
        verify(create).createRestaurant(userId, input);
        verifyNoMoreInteractions(create, update, delete, get);
    }

    @Test
    void propagatesCreateFailureWithoutCallingOtherUseCases() {
        var input = new CreateRestaurantInputDto(ownerId, "Restaurante", "Endereço", "11:00", referenceId);
        var failure = new NotAllowedException();
        when(create.createRestaurant(userId, input)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.createRestaurant(userId, input));

        assertSame(failure, error);
        verify(create).createRestaurant(userId, input);
        verifyNoMoreInteractions(create, update, delete, get);
    }

    @Test
    void delegatesUpdateWithAuthenticatedUserAndOriginalInput() {
        var input = new UpdateRestaurantInputDto(id, Optional.of("Atualizado"), Optional.empty(), Optional.empty(), Optional.empty());
        var output = new UpdateRestaurantOutputDto(id, "Atualizado", "Endereço", "11:00", referenceId);
        when(update.updateRestaurant(userId, input)).thenReturn(output);

        var result = controller.updateRestaurant(userId, input);

        assertSame(output, result);
        verify(update).updateRestaurant(userId, input);
        verifyNoMoreInteractions(create, update, delete, get);
    }

    @Test
    void propagatesUpdateFailureWithoutCallingOtherUseCases() {
        var input = new UpdateRestaurantInputDto(id, Optional.of("Atualizado"), Optional.empty(), Optional.empty(), Optional.empty());
        var failure = new NotAllowedException();
        when(update.updateRestaurant(userId, input)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.updateRestaurant(userId, input));

        assertSame(failure, error);
        verify(update).updateRestaurant(userId, input);
        verifyNoMoreInteractions(create, update, delete, get);
    }

    @Test
    void delegatesDeleteWithAuthenticatedUserAndOriginalInput() {
        assertDoesNotThrow(() -> controller.deleteRestaurant(userId, id));
        verify(delete).deleteRestaurant(userId, id);
        verifyNoMoreInteractions(create, update, delete, get);
    }

    @Test
    void propagatesDeleteFailureWithoutCallingOtherUseCases() {
        var failure = new NotAllowedException();
        doThrow(failure).when(delete).deleteRestaurant(userId, id);

        var error = assertThrows(NotAllowedException.class, () -> controller.deleteRestaurant(userId, id));

        assertSame(failure, error);
        verify(delete).deleteRestaurant(userId, id);
        verifyNoMoreInteractions(create, update, delete, get);
    }

    @Test
    void delegatesGetWithAuthenticatedUserAndOriginalInput() {
        var output = new GetRestaurantOutputDto(id, "Restaurante", "Endereço", "11:00", referenceId, Set.of(UUID.randomUUID()), Set.of(UUID.randomUUID()));
        when(get.getRestaurant(userId, id)).thenReturn(output);

        var result = controller.getRestaurant(userId, id);

        assertSame(output, result);
        verify(get).getRestaurant(userId, id);
        verifyNoMoreInteractions(create, update, delete, get);
    }

    @Test
    void propagatesGetFailureWithoutCallingOtherUseCases() {
        var failure = new NotAllowedException();
        when(get.getRestaurant(userId, id)).thenThrow(failure);

        var error = assertThrows(NotAllowedException.class, () -> controller.getRestaurant(userId, id));

        assertSame(failure, error);
        verify(get).getRestaurant(userId, id);
        verifyNoMoreInteractions(create, update, delete, get);
    }
}
