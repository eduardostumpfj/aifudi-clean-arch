package com.aifudi_clean_arch.backend.integration.web;

import com.aifudi_clean_arch.backend.integration.support.ApiIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

class OpenApiIT extends ApiIntegrationTest {
    @Test
    void publishesDocumentationForAllApisWithBasicAuthentication() {
        request().when().get("/v3/api-docs")
                .then().log().ifValidationFails().statusCode(200).contentType(ContentType.JSON)
                .body("info.title", equalTo("Aifudi Clean Arch API"))
                .body("components.securitySchemes.basicAuth.type", equalTo("http"))
                .body("components.securitySchemes.basicAuth.scheme", equalTo("basic"))
                .body("security[0]", hasKey("basicAuth"))
                .body("paths.'/v1/restaurants/register'.post.responses", hasKey("200"))
                .body("paths.'/v1/restaurants/{id}'.get.tags", hasItem("Restaurantes"))
                .body("paths.'/v1/menuitems'.post.tags", hasItem("Cardápio"))
                .body("paths.'/v1/menuitems/{id}'.delete.responses", hasKey("204"))
                .body("paths.'/v1/subroles'.post.responses", hasKey("201"))
                .body("paths.'/v1/subroles/{id}'.delete.responses", hasKey("204"))
                .body("paths.'/v1/subroles/assign'.post.tags", hasItem("Subroles"));
    }

    @Test
    void documentsResponsesWithoutGenericBodiesAndKeepsRequestExamples() {
        var docs = request().when().get("/v3/api-docs")
                .then().statusCode(200).extract().jsonPath();
        java.util.Map<String, java.util.Map<String, Object>> paths = docs.getMap("paths");
        paths.values().forEach(operations -> operations.values().forEach(value -> {
            var operation = (java.util.Map<?, ?>) value;
            var responses = (java.util.Map<?, ?>) operation.get("responses");
            responses.values().forEach(response -> {
                var documented = (java.util.Map<?, ?>) response;
                org.junit.jupiter.api.Assertions.assertFalse(documented.containsKey("content"),
                        "Responses should display only status and description");
                org.junit.jupiter.api.Assertions.assertNotNull(documented.get("description"));
            });
        }));
        org.junit.jupiter.api.Assertions.assertEquals("00000000-0000-4000-8000-000000000301",
                docs.getString("components.schemas.CreateMenuItemJson.properties.restaurantId.example"));
        org.junit.jupiter.api.Assertions.assertEquals("00000000-0000-4000-8000-000000000104",
                docs.getString("components.schemas.AssignSubRoleJson.properties.userId.example"));
        org.junit.jupiter.api.Assertions.assertNotNull(docs.getString(
                "paths.'/v1/menuitems'.post.requestBody.content.'application/json'.schema.'$ref'"));
    }

    @Test
    void servesSwaggerUiAndItsLocalConfiguration() {
        request().when().get("/swagger-ui/index.html")
                .then().log().ifValidationFails().statusCode(200)
                .contentType(ContentType.HTML).body(containsString("Swagger UI"));
        request().when().get("/v3/api-docs/swagger-config")
                .then().log().ifValidationFails().statusCode(200)
                .body("url", equalTo("/v3/api-docs"))
                .body("defaultModelsExpandDepth.toString()", equalTo("-1"))
                .body("defaultModelRendering", equalTo("example"));
    }
}
