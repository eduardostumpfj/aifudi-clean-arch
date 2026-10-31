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

class SubRoleApiIT extends ApiIntegrationTest {
    private static final String PATH = "/v1/subroles";
    private static final UUID OWNER_ROLE = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final UUID USER_ROLE = UUID.fromString("00000000-0000-4000-8000-000000000003");
    private static final UUID MAIN_OWNER = UUID.fromString("00000000-0000-4000-8000-000000000012");
    private static final UUID MAIN_USER = UUID.fromString("00000000-0000-4000-8000-000000000013");
    private static final UUID ANA = UUID.fromString("00000000-0000-4000-8000-000000000101");
    private static final UUID BRUNO = UUID.fromString("00000000-0000-4000-8000-000000000102");
    private final List<UUID> testSubroles = new ArrayList<>();
    private final List<UUID> testRestaurants = new ArrayList<>();

    @AfterEach
    void removeTestData() {
        for (UUID id : testRestaurants) {
            jdbc.update("DELETE FROM memberships WHERE restaurant_id = ?", id);
            jdbc.update("DELETE FROM restaurants WHERE id = ?", id);
        }
        for (UUID id : testSubroles) jdbc.update("DELETE FROM subroles WHERE id = ?", id);
    }

    private String uniqueValue() {
        return "it_" + UUID.randomUUID();
    }

    private String createPayload(String value, UUID roleId) {
        return "{\"value\":\"%s\",\"label\":\"Equipe de teste\",\"roleId\":\"%s\"}".formatted(value, roleId);
    }

    private String assignment(UUID user, UUID subrole, UUID restaurant) {
        return "{\"userId\":\"%s\",\"subRoleId\":\"%s\",\"restaurantId\":\"%s\"}".formatted(user, subrole, restaurant);
    }

    private UUID existingSubrole() {
        UUID id = jdbc.queryForObject("INSERT INTO subroles (role_id, value, label) VALUES (?, ?, 'Equipe de teste') RETURNING id",
                UUID.class, OWNER_ROLE, uniqueValue());
        testSubroles.add(id);
        return id;
    }

    private UUID existingRestaurant() {
        UUID id = jdbc.queryForObject("""
                INSERT INTO restaurants (kitchen_id, name, address, opening_time)
                SELECT id, 'Restaurante de subrole', 'Endereço de teste', '11:00'
                FROM kitchens WHERE value = 'BRAZILIAN' RETURNING id
                """, UUID.class);
        testRestaurants.add(id);
        return id;
    }

    private UUID existingMembership(UUID user, UUID subrole, UUID restaurant) {
        return jdbc.queryForObject("INSERT INTO memberships (user_id, subrole_id, restaurant_id) VALUES (?, ?, ?) RETURNING id",
                UUID.class, user, subrole, restaurant);
    }

    private int membershipCount(UUID restaurant) {
        return jdbc.queryForObject("SELECT count(*) FROM memberships WHERE restaurant_id = ?", Integer.class, restaurant);
    }

    @Test
    void adminCreatesSubroleWithGeneratedIdAndLocation() {
        String value = uniqueValue();
        var response = asUser("admin", "admin123").contentType(ContentType.JSON).body(createPayload(value, OWNER_ROLE))
                .when().post(PATH)
                .then().log().ifValidationFails().statusCode(201).contentType(ContentType.JSON)
                .body("id", notNullValue()).body("value", equalTo(value))
                .body("label", equalTo("Equipe de teste")).body("roleId", equalTo(OWNER_ROLE.toString()))
                .extract().response();
        UUID id = UUID.fromString(response.path("id"));
        testSubroles.add(id);
        assertEquals(PATH + "/" + id, response.header("Location"));
        assertEquals(value, jdbc.queryForObject("SELECT value FROM subroles WHERE id = ?", String.class, id));
    }

    @Test
    void adminReadsPersistedSubrole() {
        UUID id = existingSubrole();
        String value = jdbc.queryForObject("SELECT value FROM subroles WHERE id = ?", String.class, id);
        asUser("admin", "admin123").when().get(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON)
                .body("id", equalTo(id.toString())).body("value", equalTo(value))
                .body("label", equalTo("Equipe de teste")).body("roleId", equalTo(OWNER_ROLE.toString()));
    }

    @Test
    void adminPartiallyUpdatesLabelAndPreservesValueAndRole() {
        UUID id = existingSubrole();
        String value = jdbc.queryForObject("SELECT value FROM subroles WHERE id = ?", String.class, id);
        asUser("admin", "admin123").contentType(ContentType.JSON).body("{\"label\":\"Equipe atualizada\"}")
                .when().put(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200)
                .body("id", equalTo(id.toString())).body("value", equalTo(value))
                .body("label", equalTo("Equipe atualizada")).body("roleId", equalTo(OWNER_ROLE.toString()));
        assertEquals("Equipe atualizada", jdbc.queryForObject("SELECT label FROM subroles WHERE id = ?", String.class, id));
    }

    @Test
    void adminUpdatesValueLabelAndRole() {
        UUID id = existingSubrole();
        String value = uniqueValue();
        asUser("admin", "admin123").contentType(ContentType.JSON).body(createPayload(value, USER_ROLE))
                .when().put(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(200)
                .body("value", equalTo(value)).body("label", equalTo("Equipe de teste"))
                .body("roleId", equalTo(USER_ROLE.toString()));
        assertEquals(USER_ROLE, jdbc.queryForObject("SELECT role_id FROM subroles WHERE id = ?", UUID.class, id));
    }

    @Test
    void adminDeletesUnreferencedSubrole() {
        UUID id = existingSubrole();
        asUser("admin", "admin123").when().delete(PATH + "/{id}", id)
                .then().log().ifValidationFails().statusCode(204).body(isEmptyString());
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM subroles WHERE id = ?", Integer.class, id));
        asUser("admin", "admin123").when().get(PATH + "/{id}", id)
                .then().statusCode(404);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "PUT", "DELETE"})
    void missingSubroleReturnsNotFound(String method) {
        asUser("admin", "admin123").contentType(ContentType.JSON).body("{\"label\":\"Equipe\"}")
                .when().request(method, PATH + "/{id}", UUID.randomUUID())
                .then().log().ifValidationFails().statusCode(404).contentType("application/problem+json")
                .body("title", equalTo("Resource not found"));
    }

    @Test
    void missingRoleOnCreateDoesNotInsertSubrole() {
        String value = uniqueValue();
        asUser("admin", "admin123").contentType(ContentType.JSON).body(createPayload(value, UUID.randomUUID()))
                .when().post(PATH).then().log().ifValidationFails().statusCode(404);
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM subroles WHERE value = ?", Integer.class, value));
    }

    @Test
    void missingRoleOnUpdatePreservesCurrentRole() {
        UUID id = existingSubrole();
        asUser("admin", "admin123").contentType(ContentType.JSON)
                .body("{\"roleId\":\"%s\"}".formatted(UUID.randomUUID()))
                .when().put(PATH + "/{id}", id).then().log().ifValidationFails().statusCode(404);
        assertEquals(OWNER_ROLE, jdbc.queryForObject("SELECT role_id FROM subroles WHERE id = ?", UUID.class, id));
    }

    @Test
    void reservedMainOwnerCannotBeCreated() {
        asUser("admin", "admin123").contentType(ContentType.JSON).body(createPayload("mainOwner", OWNER_ROLE))
                .when().post(PATH).then().log().ifValidationFails().statusCode(403)
                .body("title", equalTo("Protected subrole"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PUT", "DELETE"})
    void mainOwnerCannotBeChangedOrDeleted(String method) {
        asUser("admin", "admin123").contentType(ContentType.JSON).body("{\"label\":\"Alteração indevida\"}")
                .when().request(method, PATH + "/{id}", MAIN_OWNER)
                .then().log().ifValidationFails().statusCode(403)
                .body("title", equalTo("Protected subrole"));
        assertEquals("mainOwner", jdbc.queryForObject("SELECT value FROM subroles WHERE id = ?", String.class, MAIN_OWNER));
    }

    @Test
    void existingSubroleCannotBeRenamedToMainOwner() {
        UUID id = existingSubrole();
        String value = jdbc.queryForObject("SELECT value FROM subroles WHERE id = ?", String.class, id);
        asUser("admin", "admin123").contentType(ContentType.JSON).body("{\"value\":\"mainOwner\"}")
                .when().put(PATH + "/{id}", id).then().log().ifValidationFails().statusCode(403);
        assertEquals(value, jdbc.queryForObject("SELECT value FROM subroles WHERE id = ?", String.class, id));
    }

    @Test
    void duplicateValueReturnsConflict() {
        UUID id = existingSubrole();
        String value = jdbc.queryForObject("SELECT value FROM subroles WHERE id = ?", String.class, id);
        asUser("admin", "admin123").contentType(ContentType.JSON).body(createPayload(value, OWNER_ROLE))
                .when().post(PATH).then().log().ifValidationFails().statusCode(409)
                .body("title", equalTo("Resource conflict"));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM subroles WHERE value = ?", Integer.class, value));
    }

    @Test
    void referencedSubroleCannotBeDeleted() {
        UUID subrole = existingSubrole();
        UUID restaurant = existingRestaurant();
        existingMembership(ANA, subrole, restaurant);
        asUser("admin", "admin123").when().delete(PATH + "/{id}", subrole)
                .then().log().ifValidationFails().statusCode(409);
        assertEquals(subrole, jdbc.queryForObject("SELECT subrole_id FROM memberships WHERE restaurant_id = ?", UUID.class, restaurant));
    }

    @Test
    void adminAssignsSubroleAndDatabaseGeneratesMembershipId() {
        UUID subrole = existingSubrole();
        UUID restaurant = existingRestaurant();
        String id = asUser("admin", "admin123").contentType(ContentType.JSON).body(assignment(ANA, subrole, restaurant))
                .when().post(PATH + "/assign")
                .then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON)
                .body("id", notNullValue()).body("userId", equalTo(ANA.toString()))
                .body("subRoleId", equalTo(subrole.toString())).body("restaurantId", equalTo(restaurant.toString()))
                .extract().path("id");
        assertEquals(UUID.fromString(id), jdbc.queryForObject("SELECT id FROM memberships WHERE restaurant_id = ?", UUID.class, restaurant));
        assertEquals(1, membershipCount(restaurant));
        assertEquals(MAIN_USER, jdbc.queryForObject("SELECT client_subrole_id FROM users WHERE id = ?", UUID.class, ANA));
    }

    @Test
    void reassignmentPreservesMembershipIdWithoutDuplicatingRecord() {
        UUID previous = existingSubrole();
        UUID replacement = existingSubrole();
        UUID restaurant = existingRestaurant();
        UUID membership = existingMembership(ANA, previous, restaurant);
        asUser("admin", "admin123").contentType(ContentType.JSON).body(assignment(ANA, replacement, restaurant))
                .when().post(PATH + "/assign")
                .then().log().ifValidationFails().statusCode(200)
                .body("id", equalTo(membership.toString())).body("subRoleId", equalTo(replacement.toString()));
        assertEquals(1, membershipCount(restaurant));
        assertEquals(replacement, jdbc.queryForObject("SELECT subrole_id FROM memberships WHERE id = ?", UUID.class, membership));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user", "subrole", "restaurant"})
    void missingAssignmentDependencyDoesNotCreateMembership(String dependency) {
        UUID subrole = existingSubrole();
        UUID restaurant = existingRestaurant();
        UUID user = dependency.equals("user") ? UUID.randomUUID() : ANA;
        UUID assigned = dependency.equals("subrole") ? UUID.randomUUID() : subrole;
        UUID targetRestaurant = dependency.equals("restaurant") ? UUID.randomUUID() : restaurant;
        asUser("admin", "admin123").contentType(ContentType.JSON).body(assignment(user, assigned, targetRestaurant))
                .when().post(PATH + "/assign").then().log().ifValidationFails().statusCode(404);
        assertEquals(0, membershipCount(restaurant));
    }

    @Test
    void cannotAssignProtectedMainOwner() {
        UUID restaurant = existingRestaurant();
        asUser("admin", "admin123").contentType(ContentType.JSON).body(assignment(ANA, MAIN_OWNER, restaurant))
                .when().post(PATH + "/assign").then().log().ifValidationFails().statusCode(403);
        assertEquals(0, membershipCount(restaurant));
    }

    @Test
    void cannotReplaceExistingMainOwnerMembership() {
        UUID subrole = existingSubrole();
        UUID restaurant = existingRestaurant();
        UUID membership = existingMembership(BRUNO, MAIN_OWNER, restaurant);
        asUser("admin", "admin123").contentType(ContentType.JSON).body(assignment(BRUNO, subrole, restaurant))
                .when().post(PATH + "/assign").then().log().ifValidationFails().statusCode(403);
        assertEquals(MAIN_OWNER, jdbc.queryForObject("SELECT subrole_id FROM memberships WHERE id = ?", UUID.class, membership));
    }

    @ParameterizedTest
    @CsvSource({
            "brunosantos, bruno123, create", "brunosantos, bruno123, get", "brunosantos, bruno123, update",
            "brunosantos, bruno123, delete", "brunosantos, bruno123, assign",
            "anasilva, ana123, create", "anasilva, ana123, get", "anasilva, ana123, update",
            "anasilva, ana123, delete", "anasilva, ana123, assign"
    })
    void nonAdminsCannotManageSubroles(String login, String password, String operation) {
        var request = asUser(login, password).contentType(ContentType.JSON);
        var response = switch (operation) {
            case "create" -> request.body(createPayload(uniqueValue(), OWNER_ROLE)).when().post(PATH);
            case "get" -> request.when().get(PATH + "/{id}", MAIN_USER);
            case "update" -> request.body("{\"label\":\"Indevido\"}").when().put(PATH + "/{id}", MAIN_USER);
            case "delete" -> request.when().delete(PATH + "/{id}", MAIN_USER);
            default -> request.body(assignment(ANA, MAIN_USER, UUID.randomUUID())).when().post(PATH + "/assign");
        };
        response.then().log().ifValidationFails().statusCode(403);
    }

    @Test
    void missingAuthorizationReturnsUnauthorized() {
        request().when().get(PATH + "/{id}", MAIN_USER)
                .then().log().ifValidationFails().statusCode(401)
                .header("WWW-Authenticate", equalTo("Basic realm=\"aifudi\", charset=\"UTF-8\""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/assign"})
    void emptyJsonReturnsValidationFailure(String endpoint) {
        asUser("admin", "admin123").contentType(ContentType.JSON).body("{}")
                .when().post(PATH + endpoint).then().log().ifValidationFails().statusCode(400)
                .body("title", equalTo("Validation failed"));
    }

    @Test
    void malformedJsonReturnsInvalidRequestBody() {
        asUser("admin", "admin123").contentType(ContentType.JSON).body("")
                .when().post(PATH).then().log().ifValidationFails().statusCode(400)
                .body("title", equalTo("Invalid request body"));
    }
}
