package com.aifudi_clean_arch.backend.integration.support;

import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.jpa.hibernate.ddl-auto=validate", "spring.sql.init.mode=never"})
@ActiveProfiles("integration")
public abstract class ApiIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void verifyTestDatabase() {
        assertEquals("aifudi_integration", jdbc.queryForObject("SELECT current_database()", String.class),
                "Os testes devem usar exclusivamente o banco aifudi_integration");
    }

    protected RequestSpecification request() {
        return given().baseUri("http://localhost").port(port);
    }

    protected RequestSpecification asUser(String login, String password) {
        return request().auth().preemptive().basic(login, password);
    }
}
