package com.aifudi_clean_arch.backend.infra.database.jpa.gateway;

import com.aifudi_clean_arch.backend.core.domain.*;
import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.infra.database.jpa.entity.*;
import com.aifudi_clean_arch.backend.infra.database.jpa.repository.*;
import com.aifudi_clean_arch.backend.infra.database.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserJpaGatewayTest {
    private final UserRepository repository = mock(UserRepository.class, withSettings().mockMaker("mock-maker-subclass"));
    private final UserJpaGateway gateway = new UserJpaGateway(repository, new UserMapper(new SubRoleMapper(new RoleMapper())));
    private final UUID id = UUID.randomUUID();
    private final SubRoleEntity subrole = new SubRoleEntity(UUID.randomUUID(), new RoleEntity(UUID.randomUUID(), "USER", "Cliente"), "mainUser", "Cliente");
    private final UserEntity entity = new UserEntity(id, subrole, "Ana", "ana", "ana@example.com", "hash", "Endereço");

    @ParameterizedTest
    @ValueSource(strings = {"id", "login"})
    void mapsFoundUserWithClientSubrole(String lookup) {
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.findByLogin("ana")).thenReturn(Optional.of(entity));
        var user = (lookup.equals("id") ? gateway.getUserById(id) : gateway.getUserByLogin("ana")).orElseThrow();
        assertAll(() -> assertEquals(id, user.getId()),
                () -> assertEquals("Ana", user.getName()),
                () -> assertEquals("ana", user.getLogin()),
                () -> assertEquals("ana@example.com", user.getEmail()),
                () -> assertEquals("hash", user.getHashedPassword()),
                () -> assertEquals("Endereço", user.getAddress()),
                () -> assertEquals(subrole.getId(), user.getClientSubrole().getId()),
                () -> assertEquals("USER", user.getClientSubrole().getRole().getValue()));
        if (lookup.equals("id")) verify(repository).findById(id);
        else verify(repository).findByLogin("ana");
    }

    @Test
    void returnsEmptyForMissingIdAndLogin() {
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.findByLogin("ana")).thenReturn(Optional.empty());
        assertTrue(gateway.getUserById(id).isEmpty());
        assertTrue(gateway.getUserByLogin("ana").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "login"})
    void wrapsLookupFailuresAndPreservesCause(String lookup) {
        var failure = new DataAccessResourceFailureException("offline");
        when(repository.findById(id)).thenThrow(failure);
        when(repository.findByLogin("ana")).thenThrow(failure);
        var error = assertThrows(ErrorOnRepositoryAccess.class,
                () -> { if (lookup.equals("id")) gateway.getUserById(id); else gateway.getUserByLogin("ana"); });
        assertSame(failure, error.getCause());
        assertTrue(error.getMessage().contains(lookup.equals("id") ? id.toString() : "ana"));
    }
}
