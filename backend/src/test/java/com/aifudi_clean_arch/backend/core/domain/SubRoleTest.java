package com.aifudi_clean_arch.backend.core.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SubRoleTest {
    private final Role owner = new Role(UUID.randomUUID(), "OWNER", "Proprietário");

    @Test
    void allowsNewSubroleWithoutId() {
        var subrole = new SubRole(null, "staff", "Equipe", owner);
        assertAll(() -> assertNull(subrole.getId()),
                () -> assertEquals("staff", subrole.getValue()),
                () -> assertEquals("Equipe", subrole.getLabel()),
                () -> assertSame(owner, subrole.getRole()));
    }

    @Test
    void identifiesMainOwnerOnlyWhenBothValueAndRoleMatch() {
        var id = UUID.randomUUID();
        var mainOwner = new SubRole(id, "mainOwner", "Principal", owner);
        var user = new Role(UUID.randomUUID(), "USER", "Cliente");
        assertAll(() -> assertEquals(id, mainOwner.getId()),
                () -> assertTrue(mainOwner.isMainOwner()),
                () -> assertFalse(new SubRole(id, "staff", "Equipe", owner).isMainOwner()),
                () -> assertFalse(new SubRole(id, "mainOwner", "Principal", user).isMainOwner()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidValue(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new SubRole(null, invalid, "Equipe", owner));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void rejectsInvalidLabel(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new SubRole(null, "staff", invalid, owner));
    }

    @Test
    void rejectsMissingRole() {
        assertThrows(IllegalArgumentException.class, () -> new SubRole(null, "staff", "Equipe", null));
    }
}
