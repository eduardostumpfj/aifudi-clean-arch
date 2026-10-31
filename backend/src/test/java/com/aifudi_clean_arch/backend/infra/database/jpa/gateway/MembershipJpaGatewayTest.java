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

class MembershipJpaGatewayTest {
    private final MembershipRepository repository = mock(MembershipRepository.class, withSettings().mockMaker("mock-maker-subclass"));
    private final SubRoleMapper subroleMapper = new SubRoleMapper(new RoleMapper());
    private final MembershipMapper mapper = new MembershipMapper(new UserMapper(subroleMapper), subroleMapper, new RestaurantMapper(new KitchenMapper()));
    private final MembershipJpaGateway gateway = new MembershipJpaGateway(repository, mapper);
    private final SubRole subrole = new SubRole(UUID.randomUUID(), "staff", "Equipe", new Role(UUID.randomUUID(), "OWNER", "Dono"));
    private final User user = new User(UUID.randomUUID(), "Ana", "ana@example.com", "ana", "hash", "Endereço", subrole);
    private final Restaurant restaurant = new Restaurant(UUID.randomUUID(), "Restaurante", "Endereço", "11:00",
            new Kitchen(UUID.randomUUID(), "BRAZILIAN", "Brasileira"), Set.of(), Set.of());
    private final Membership membership = new Membership(UUID.randomUUID(), user, subrole, restaurant);

    private Optional<Membership> lookup(String mode) {
        return switch (mode) {
            case "user" -> gateway.getUserMembership(user.getId());
            case "userRestaurant" -> gateway.getUserRestaurantMembership(user.getId(), restaurant.getId());
            default -> gateway.getUserRestaurantMembership(membership);
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "userRestaurant", "membership"})
    void mapsMembershipAndAssociations(String mode) {
        var entity = mapper.map(membership);
        when(repository.findByUser_Id(user.getId())).thenReturn(Optional.of(entity));
        when(repository.findByUser_IdAndRestaurant_Id(user.getId(), restaurant.getId())).thenReturn(Optional.of(entity));
        var found = lookup(mode).orElseThrow();
        assertAll(() -> assertEquals(membership.getId(), found.getId()),
                () -> assertEquals(user.getId(), found.getUser().getId()),
                () -> assertEquals(subrole.getId(), found.getSubrole().getId()),
                () -> assertEquals(restaurant.getId(), found.getRestaurant().getId()));
        if (mode.equals("user")) verify(repository).findByUser_Id(user.getId());
        else verify(repository).findByUser_IdAndRestaurant_Id(user.getId(), restaurant.getId());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "userRestaurant", "membership"})
    void returnsEmptyWhenMembershipIsMissing(String mode) {
        when(repository.findByUser_Id(user.getId())).thenReturn(Optional.empty());
        when(repository.findByUser_IdAndRestaurant_Id(user.getId(), restaurant.getId())).thenReturn(Optional.empty());
        assertTrue(lookup(mode).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "userRestaurant", "membership"})
    void wrapsLookupFailuresAndPreservesCause(String mode) {
        var failure = new DataAccessResourceFailureException("offline");
        when(repository.findByUser_Id(user.getId())).thenThrow(failure);
        when(repository.findByUser_IdAndRestaurant_Id(user.getId(), restaurant.getId())).thenThrow(failure);
        assertSame(failure, assertThrows(ErrorOnRepositoryAccess.class, () -> lookup(mode)).getCause());
    }

    @Test
    void savesNewMembershipAndReturnsGeneratedId() {
        var generatedId = UUID.randomUUID();
        var pending = new Membership(null, user, subrole, restaurant);
        when(repository.save(any())).thenAnswer(i -> {
            MembershipEntity entity = i.getArgument(0);
            assertNull(entity.getId());
            entity.setId(generatedId);
            return entity;
        });
        var saved = gateway.save(pending);
        assertAll(() -> assertEquals(generatedId, saved.getId()),
                () -> assertEquals(user.getId(), saved.getUser().getId()),
                () -> assertEquals(subrole.getId(), saved.getSubrole().getId()),
                () -> assertEquals(restaurant.getId(), saved.getRestaurant().getId()));
        verify(repository).save(argThat(e -> e.getUser().getId().equals(user.getId())
                && e.getSubrole().getId().equals(subrole.getId())
                && e.getRestaurant().getId().equals(restaurant.getId())));
    }

    @Test
    void wrapsSaveFailureAndPreservesCause() {
        var failure = new DataAccessResourceFailureException("offline");
        when(repository.save(any())).thenThrow(failure);
        assertSame(failure, assertThrows(ErrorOnRepositoryAccess.class, () -> gateway.save(membership)).getCause());
    }
}
