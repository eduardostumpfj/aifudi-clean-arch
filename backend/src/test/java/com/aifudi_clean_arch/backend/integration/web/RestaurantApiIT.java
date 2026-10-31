package com.aifudi_clean_arch.backend.integration.web;

import com.aifudi_clean_arch.backend.integration.support.ApiIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantApiIT extends ApiIntegrationTest {
    private static final String PATH = "/v1/restaurants";
    private static final UUID OWNER = UUID.fromString("00000000-0000-4000-8000-000000000102");
    private static final UUID KITCHEN = UUID.fromString("00000000-0000-4000-8000-000000000203");
    private static final UUID ITALIAN_KITCHEN = UUID.fromString("00000000-0000-4000-8000-000000000201");
    private final List<UUID> testRestaurants = new ArrayList<>();

    @AfterEach
    void removeTestRestaurants() {
        // Remove apenas os registros criados pelo próprio cenário, na ordem das FKs.
        for (UUID id : testRestaurants) {
            jdbc.update("DELETE FROM menu_items WHERE restaurant_id = ?", id);
            jdbc.update("DELETE FROM memberships WHERE restaurant_id = ?", id);
            jdbc.update("DELETE FROM restaurants WHERE id = ?", id);
        }
    }

    private String payload(UUID ownerId, UUID kitchenId) {
        return """
                {"ownerId":"%s","name":"Restaurante de integração","address":"Rua de teste, 10",
                 "openingTime":"11:00 - 22:00","kitchenId":"%s"}
                """.formatted(ownerId, kitchenId);
    }

    private UUID existingRestaurant(boolean withOwner) {
        UUID id = jdbc.queryForObject("""
                INSERT INTO restaurants (kitchen_id, name, address, opening_time)
                VALUES (?, 'Restaurante de integração', 'Rua de teste, 10', '11:00 - 22:00') RETURNING id
                """, UUID.class, KITCHEN);
        testRestaurants.add(id);
        if (withOwner) {
            jdbc.update("""
                    INSERT INTO memberships (user_id, subrole_id, restaurant_id)
                    SELECT ?, id, ? FROM subroles WHERE value = 'mainOwner'
                    """, OWNER, id);
        }
        return id;
    }

    private int restaurantCount() {
        return jdbc.queryForObject("SELECT count(*) FROM restaurants", Integer.class);
    }

    @ParameterizedTest
    @CsvSource({"brunosantos, bruno123", "admin, admin123"})
    void createsRestaurantWithGeneratedIdAndMainOwnerMembership(String login, String password) {
        int countBefore = restaurantCount();
        String createdId = asUser(login, password)
                .contentType(ContentType.JSON).body(payload(OWNER, KITCHEN))
                .when().post(PATH + "/register")
                .then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo("Restaurante de integração"))
                .body("address", equalTo("Rua de teste, 10"))
                .body("openingTime", equalTo("11:00 - 22:00"))
                .body("kitchenId", equalTo(KITCHEN.toString()))
                .body("ownerId", equalTo(OWNER.toString()))
                .extract().path("id");
        UUID id = UUID.fromString(createdId);
        testRestaurants.add(id);

        assertEquals(countBefore + 1, restaurantCount());
        assertEquals("Restaurante de integração", jdbc.queryForObject("SELECT name FROM restaurants WHERE id = ?", String.class, id));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM memberships WHERE restaurant_id = ?", Integer.class, id));
        assertEquals(OWNER, jdbc.queryForObject("SELECT user_id FROM memberships WHERE restaurant_id = ?", UUID.class, id));
        assertEquals("mainOwner", jdbc.queryForObject("""
                SELECT s.value FROM memberships m JOIN subroles s ON s.id = m.subrole_id
                WHERE m.restaurant_id = ?
                """, String.class, id));
    }

    @Test
    void clientReadsRestaurantWithMembershipAndMenuItemIds() {
        UUID id = existingRestaurant(true);
        UUID membershipId = jdbc.queryForObject("SELECT id FROM memberships WHERE restaurant_id = ?", UUID.class, id);
        UUID itemId = jdbc.queryForObject("""
                INSERT INTO menu_items (restaurant_id, name, description, price, picture, has_delivery)
                VALUES (?, 'Item', 'Descrição', 10.00, 'item.jpg', false) RETURNING id
                """, UUID.class, id);

        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON)
                .body("id", equalTo(id.toString()))
                .body("name", equalTo("Restaurante de integração"))
                .body("address", equalTo("Rua de teste, 10"))
                .body("openingTime", equalTo("11:00 - 22:00"))
                .body("kitchenId", equalTo(KITCHEN.toString()))
                .body("membershipIds", contains(membershipId.toString()))
                .body("menuItemsIdList", contains(itemId.toString()));
    }

    @Test
    void ownerPartiallyUpdatesRestaurantAndPreservesOtherFields() {
        UUID id = existingRestaurant(true);
        asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body("{\"name\":\"Nome atualizado\"}")
                .when().put(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200)
                .body("id", equalTo(id.toString()))
                .body("name", equalTo("Nome atualizado"))
                .body("address", equalTo("Rua de teste, 10"))
                .body("openingTime", equalTo("11:00 - 22:00"))
                .body("kitchenId", equalTo(KITCHEN.toString()));
        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", id)
                .then().statusCode(200).body("name", equalTo("Nome atualizado"))
                .body("membershipIds", hasSize(1));
    }

    @Test
    void adminUpdatesAllFieldsIncludingKitchen() {
        UUID id = existingRestaurant(false);
        asUser("admin", "admin123")
                .contentType(ContentType.JSON).body("""
                        {"name":"Restaurante italiano","address":"Novo endereço",
                         "openingTime":"18:00 - 23:00","kitchenId":"%s"}
                        """.formatted(ITALIAN_KITCHEN))
                .when().put(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200)
                .body("name", equalTo("Restaurante italiano"))
                .body("address", equalTo("Novo endereço"))
                .body("openingTime", equalTo("18:00 - 23:00"))
                .body("kitchenId", equalTo(ITALIAN_KITCHEN.toString()));
        assertEquals(ITALIAN_KITCHEN, jdbc.queryForObject("SELECT kitchen_id FROM restaurants WHERE id = ?", UUID.class, id));
    }

    @Test
    void adminDeletesRestaurantWithoutRelatedRecords() {
        UUID id = existingRestaurant(false);
        asUser("admin", "admin123")
                .when().delete(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200).body(isEmptyString());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM restaurants WHERE id = ?", Integer.class, id));
        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", id)
                .then().statusCode(404);
    }

    @Test
    void deletionWithExistingMembershipReturnsConflictAndPreservesData() {
        UUID id = existingRestaurant(true);
        asUser("brunosantos", "bruno123")
                .when().delete(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(409)
                .contentType("application/problem+json")
                .body("title", equalTo("Resource conflict"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM restaurants WHERE id = ?", Integer.class, id));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM memberships WHERE restaurant_id = ?", Integer.class, id));
    }

    @Test
    void getMissingRestaurantReturnsProblemDetail() {
        UUID id = UUID.randomUUID();
        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(404)
                .contentType("application/problem+json")
                .body("title", equalTo("Resource not found"))
                .body("instance", equalTo(PATH + "/" + id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"owner", "kitchen"})
    void missingCreationDependencyDoesNotInsertRestaurant(String dependency) {
        int before = restaurantCount();
        UUID owner = dependency.equals("owner") ? UUID.randomUUID() : OWNER;
        UUID kitchen = dependency.equals("kitchen") ? UUID.randomUUID() : KITCHEN;
        asUser("admin", "admin123")
                .contentType(ContentType.JSON).body(payload(owner, kitchen))
                .when().post(PATH + "/register")
                .then().log().ifValidationFails().statusCode(404);
        assertEquals(before, restaurantCount());
    }

    @Test
    void clientCannotCreateRestaurant() {
        int before = restaurantCount();
        asUser("anasilva", "ana123")
                .contentType(ContentType.JSON).body(payload(OWNER, KITCHEN))
                .when().post(PATH + "/register")
                .then().log().ifValidationFails().statusCode(403);
        assertEquals(before, restaurantCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE"})
    void anotherOwnerCannotChangeRestaurant(String method) {
        UUID id = existingRestaurant(true);
        asUser("carlaoliveira", "carla123")
                .contentType(ContentType.JSON).body("{\"name\":\"Indevido\"}")
                .when().request(method, PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(403);
        assertEquals("Restaurante de integração", jdbc.queryForObject("SELECT name FROM restaurants WHERE id = ?", String.class, id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE"})
    void clientMembershipDoesNotAuthorizeManagement(String method) {
        UUID id = existingRestaurant(false);
        jdbc.update("""
                INSERT INTO memberships (user_id, subrole_id, restaurant_id)
                SELECT u.id, s.id, ? FROM users u CROSS JOIN subroles s
                WHERE u.login = 'anasilva' AND s.value = 'mainUser'
                """, id);
        asUser("anasilva", "ana123")
                .contentType(ContentType.JSON).body("{\"name\":\"Indevido\"}")
                .when().request(method, PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(403);
        assertEquals("Restaurante de integração", jdbc.queryForObject("SELECT name FROM restaurants WHERE id = ?", String.class, id));
    }

    @Test
    void unauthenticatedGetReturnsUnauthorized() {
        request().when().get(PATH + "/{id}", UUID.randomUUID())
                .then().log().ifValidationFails().statusCode(401)
                .header("WWW-Authenticate", equalTo("Basic realm=\"aifudi\", charset=\"UTF-8\""));
    }

    @Test
    void emptyJsonReturnsValidationFailure() {
        int before = restaurantCount();
        asUser("admin", "admin123").contentType(ContentType.JSON).body("{}")
                .when().post(PATH + "/register")
                .then().log().ifValidationFails().statusCode(400)
                .body("title", equalTo("Validation failed"));
        assertEquals(before, restaurantCount());
    }

    @Test
    void malformedJsonReturnsInvalidRequestBody() {
        int before = restaurantCount();
        asUser("admin", "admin123").contentType(ContentType.JSON).body("")
                .when().post(PATH + "/register")
                .then().log().ifValidationFails().statusCode(400)
                .body("title", equalTo("Invalid request body"));
        assertEquals(before, restaurantCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE"})
    void adminChangingMissingRestaurantReceivesNotFound(String method) {
        asUser("admin", "admin123").contentType(ContentType.JSON).body("{\"name\":\"Novo\"}")
                .when().request(method, PATH + "/{id}", UUID.randomUUID())
                .then().log().ifValidationFails().statusCode(404);
    }

    @Test
    void missingKitchenOnUpdatePreservesPreviousKitchen() {
        UUID id = existingRestaurant(true);
        asUser("brunosantos", "bruno123").contentType(ContentType.JSON)
                .body("{\"kitchenId\":\"%s\"}".formatted(UUID.randomUUID()))
                .when().put(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(404);
        assertEquals(KITCHEN, jdbc.queryForObject("SELECT kitchen_id FROM restaurants WHERE id = ?", UUID.class, id));
    }
}
