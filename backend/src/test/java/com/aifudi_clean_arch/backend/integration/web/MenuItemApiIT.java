package com.aifudi_clean_arch.backend.integration.web;

import com.aifudi_clean_arch.backend.integration.support.ApiIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class MenuItemApiIT extends ApiIntegrationTest {
    private static final UUID BRUNO_RESTAURANT = UUID.fromString("00000000-0000-4000-8000-000000000301");
    private static final UUID CARLA_RESTAURANT = UUID.fromString("00000000-0000-4000-8000-000000000302");
    private static final String PATH = "/v1/menuitems";

    @BeforeEach
    void clearMenuItems() {
        // Os demais registros do init.sql são compartilhados e não são alterados pelos testes.
        jdbc.update("DELETE FROM menu_items");
    }

    private String payload(UUID restaurantId, BigDecimal price) {
        return """
                {"name":"Hambúrguer","description":"Pão, carne e queijo",
                 "price":%s,"has_delivery":true,"picture":"burger.jpg","restaurantId":"%s"}
                """.formatted(price, restaurantId);
    }

    private UUID existingItem(UUID restaurantId) {
        return jdbc.queryForObject("""
                INSERT INTO menu_items (restaurant_id, name, description, price, picture, has_delivery)
                VALUES (?, 'Hambúrguer', 'Pão, carne e queijo', 29.90, 'burger.jpg', true) RETURNING id
                """, UUID.class, restaurantId);
    }

    private int itemCount() {
        return jdbc.queryForObject("SELECT count(*) FROM menu_items", Integer.class);
    }

    @Test
    void ownerCreatesItemWithDatabaseGeneratedId() {
        String id = asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body(payload(BRUNO_RESTAURANT, new BigDecimal("29.90")))
                .when().post(PATH)
                .then().log().ifValidationFails()
                .statusCode(200).contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("name", equalTo("Hambúrguer"))
                .body("description", equalTo("Pão, carne e queijo"))
                .body("price", equalTo(29.90f))
                .body("has_delivery", equalTo(true))
                .body("picture", equalTo("burger.jpg"))
                .body("restaurantId", equalTo(BRUNO_RESTAURANT.toString()))
                .extract().path("id");

        UUID generatedId = UUID.fromString(id);
        assertEquals(1, itemCount());
        assertEquals("Hambúrguer", jdbc.queryForObject("SELECT name FROM menu_items WHERE id = ?", String.class, generatedId));
        assertEquals(BRUNO_RESTAURANT, jdbc.queryForObject("SELECT restaurant_id FROM menu_items WHERE id = ?", UUID.class, generatedId));
    }

    @Test
    void adminCreatesItemInAnyRestaurant() {
        asUser("admin", "admin123")
                .contentType(ContentType.JSON).body(payload(CARLA_RESTAURANT, BigDecimal.TEN))
                .when().post(PATH)
                .then().log().ifValidationFails().statusCode(200)
                .body("restaurantId", equalTo(CARLA_RESTAURANT.toString()));
        assertEquals(1, itemCount());
    }

    @Test
    void clientReadsPersistedItem() {
        UUID id = existingItem(BRUNO_RESTAURANT);
        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON)
                .body("id", equalTo(id.toString()))
                .body("name", equalTo("Hambúrguer"))
                .body("price", equalTo(29.90f))
                .body("restaurantId", equalTo(BRUNO_RESTAURANT.toString()));
    }

    @Test
    void ownerUpdatesOnlyProvidedFieldsAndPersistsFalseDelivery() {
        UUID id = existingItem(BRUNO_RESTAURANT);
        asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body("""
                        {"name":"Hambúrguer especial","has_delivery":false}
                        """)
                .when().put(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200)
                .body("id", equalTo(id.toString()))
                .body("name", equalTo("Hambúrguer especial"))
                .body("description", equalTo("Pão, carne e queijo"))
                .body("price", equalTo(29.90f))
                .body("picture", equalTo("burger.jpg"))
                .body("has_delivery", equalTo(false))
                .body("restaurantId", equalTo(BRUNO_RESTAURANT.toString()));

        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", id)
                .then().statusCode(200)
                .body("name", equalTo("Hambúrguer especial"))
                .body("has_delivery", equalTo(false));
        assertEquals(1, itemCount());
    }

    @Test
    void ownerDeletesItemAndSubsequentGetReturnsNotFound() {
        UUID id = existingItem(BRUNO_RESTAURANT);
        asUser("brunosantos", "bruno123")
                .when().delete(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(204).body(isEmptyString());
        assertEquals(0, itemCount());
        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", id)
                .then().statusCode(404).body("status", equalTo(404));
    }

    @Test
    void missingItemReturnsProblemDetail() {
        UUID missingId = UUID.randomUUID();
        asUser("anasilva", "ana123")
                .when().get(PATH + "/{id}", missingId)
                .then().log().ifValidationFails().statusCode(404)
                .contentType("application/problem+json")
                .body("title", equalTo("Resource not found"))
                .body("instance", equalTo(PATH + "/" + missingId));
    }

    @Test
    void ownerCannotCreateItemInAnotherOwnersRestaurant() {
        asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body(payload(CARLA_RESTAURANT, BigDecimal.TEN))
                .when().post(PATH)
                .then().log().ifValidationFails().statusCode(403);
        assertEquals(0, itemCount());
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE"})
    void ownerCannotChangeAnotherOwnersItem(String method) {
        UUID id = existingItem(CARLA_RESTAURANT);
        asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body("{\"name\":\"Alterado\"}")
                .when().request(method, PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(403);
        assertEquals(1, itemCount());
        assertEquals("Hambúrguer", jdbc.queryForObject("SELECT name FROM menu_items WHERE id = ?", String.class, id));
    }

    @Test
    void clientCannotCreateItemEvenWithMembership() {
        asUser("anasilva", "ana123")
                .contentType(ContentType.JSON).body(payload(BRUNO_RESTAURANT, BigDecimal.TEN))
                .when().post(PATH)
                .then().log().ifValidationFails().statusCode(403);
        assertEquals(0, itemCount());
    }

    @Test
    void missingAuthorizationReturnsBasicChallenge() {
        request()
                .when().get(PATH + "/{id}", UUID.randomUUID())
                .then().log().ifValidationFails().statusCode(401)
                .header("WWW-Authenticate", equalTo("Basic realm=\"aifudi\", charset=\"UTF-8\""))
                .contentType("application/problem+json")
                .body("title", equalTo("Authentication failed"));
    }

    @Test
    void wrongPasswordReturnsUnauthorized() {
        asUser("brunosantos", "wrong-password")
                .when().get(PATH + "/{id}", UUID.randomUUID())
                .then().log().ifValidationFails().statusCode(401);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void invalidCreatePriceIsRejectedBeforeInsertion(String price) {
        asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body(payload(BRUNO_RESTAURANT, new BigDecimal(price)))
                .when().post(PATH)
                .then().log().ifValidationFails().statusCode(400)
                .body("title", equalTo("Validation failed"));
        assertEquals(0, itemCount());
    }

    @Test
    void invalidUpdatePriceDoesNotChangePersistedItem() {
        UUID id = existingItem(BRUNO_RESTAURANT);
        asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body("{\"price\":0}")
                .when().put(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(400);
        assertEquals(new BigDecimal("29.90"), jdbc.queryForObject("SELECT price FROM menu_items WHERE id = ?", BigDecimal.class, id));
    }

    @Test
    void missingRestaurantReturnsNotFoundForAdmin() {
        asUser("admin", "admin123")
                .contentType(ContentType.JSON).body(payload(UUID.randomUUID(), BigDecimal.TEN))
                .when().post(PATH)
                .then().log().ifValidationFails().statusCode(404);
        assertEquals(0, itemCount());
    }

    @Test
    void malformedJsonReturnsBadRequest() {
        asUser("brunosantos", "bruno123")
                .contentType(ContentType.JSON).body("")
                .when().post(PATH)
                .then().log().ifValidationFails().statusCode(400)
                .body("title", equalTo("Invalid request body"));
        assertEquals(0, itemCount());
    }
}
