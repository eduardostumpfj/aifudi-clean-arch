package com.aifudi_clean_arch.backend.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private final UUID id = UUID.randomUUID();
    private final SubRole subrole = new SubRole(UUID.randomUUID(), "mainUser", "Cliente", new Role(UUID.randomUUID(), "USER", "Usuário"));

    @Test
    void preservesValidUserData() {
        var user = new User(id, "Ana", "ana@example.com", "ana", "hash", "Endereço", subrole);
        assertAll(() -> assertEquals(id, user.getId()),
                () -> assertEquals("Ana", user.getName()),
                () -> assertEquals("ana@example.com", user.getEmail()),
                () -> assertEquals("ana", user.getLogin()),
                () -> assertEquals("hash", user.getHashedPassword()),
                () -> assertEquals("Endereço", user.getAddress()),
                () -> assertSame(subrole, user.getClientSubrole()));
    }

    @Test
    void rejectsMissingId() {
        assertThrows(IllegalArgumentException.class, () -> new User(null, "Ana", "ana@example.com", "ana", "hash", "Endereço", subrole));
    }

    @Test
    void rejectsMissingClientSubrole() {
        assertThrows(IllegalArgumentException.class, () -> new User(id, "Ana", "ana@example.com", "ana", "hash", "Endereço", null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidName(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new User(id, invalid, "ana@example.com", "ana", "hash", "Endereço", subrole));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidEmail(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new User(id, "Ana", invalid, "ana", "hash", "Endereço", subrole));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidLogin(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new User(id, "Ana", "ana@example.com", invalid, "hash", "Endereço", subrole));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidHashedPassword(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new User(id, "Ana", "ana@example.com", "ana", invalid, "Endereço", subrole));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidAddress(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new User(id, "Ana", "ana@example.com", "ana", "hash", invalid, subrole));
    }
}
