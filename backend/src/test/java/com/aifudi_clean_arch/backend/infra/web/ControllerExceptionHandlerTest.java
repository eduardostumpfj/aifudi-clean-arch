package com.aifudi_clean_arch.backend.infra.web;

import com.aifudi_clean_arch.backend.core.exception.*;
import com.aifudi_clean_arch.backend.infra.web.handler.ControllerExceptionHandler;
import com.aifudi_clean_arch.backend.infra.web.json.CreateSubRoleJson;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ControllerExceptionHandlerTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ErrorController())
            .setControllerAdvice(new ControllerExceptionHandler()).build();

    @RestController
    static class ErrorController {
        @GetMapping("/test/errors/{kind}")
        public void fail(@PathVariable("kind") String kind) {
            switch (kind) {
                case "auth" -> throw new InvalidCredentialsException();
                case "permission" -> throw new NotAllowedException();
                case "missing" -> throw new RestaurantNotFoundException(UUID.randomUUID());
                case "protected" -> throw new ProtectedSubRoleException("main");
                case "price" -> throw new MenuItemInvalidPrice(UUID.randomUUID());
                case "conflict" -> throw new DataIntegrityViolationException("secret SQL");
                case "wrapped-conflict" -> throw new ErrorOnRepositoryAccess("database details",
                        new DataIntegrityViolationException("secret SQL"));
                case "repository" -> throw new ErrorOnRepositoryAccess("database details", new RuntimeException("secret"));
                default -> throw new IllegalStateException("internal secret");
            }
        }

        @PostMapping("/test/validation")
        public void validate(@Valid @RequestBody CreateSubRoleJson input) {
        }

        @GetMapping("/test/uuid/{id}")
        public void uuid(@PathVariable("id") UUID id) {
        }
    }

    @Test
    void authenticationReturnsProblemDetailAndBasicChallenge() throws Exception {
        mvc.perform(get("/test/errors/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"aifudi\", charset=\"UTF-8\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Authentication failed"))
                .andExpect(jsonPath("$.detail").value("Invalid credentials"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").value("/test/errors/auth"));
    }

    @Test
    void mapsDomainErrorsToHttpStatuses() throws Exception {
        for (String kind : new String[]{"permission", "protected"}) {
            mvc.perform(get("/test/errors/" + kind)).andExpect(status().isForbidden());
        }
        mvc.perform(get("/test/errors/missing")).andExpect(status().isNotFound());
        mvc.perform(get("/test/errors/price")).andExpect(status().isBadRequest());
    }

    @Test
    void conflictsAndUnexpectedErrorsDoNotExposeInternalDetails() throws Exception {
        for (String kind : new String[]{"conflict", "wrapped-conflict"}) {
            mvc.perform(get("/test/errors/" + kind)).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail").value("The operation conflicts with existing data or related resources."));
        }
        for (String kind : new String[]{"repository", "unexpected"}) {
            mvc.perform(get("/test/errors/" + kind)).andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.detail").value("An unexpected error occurred. Please try again later."));
        }
    }

    @Test
    void validationReturnsFieldErrorsWithoutRejectedValues() throws Exception {
        mvc.perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.instance").value("/test/validation"));
    }

    @Test
    void malformedBodiesParametersAndUnsupportedMethodsKeepClientErrorStatus() throws Exception {
        mvc.perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.title").value("Invalid request body"));
        mvc.perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":\"staff\",\"label\":\"Staff\",\"roleId\":\"invalid\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/test/uuid/invalid")).andExpect(status().isBadRequest());
        mvc.perform(delete("/test/validation")).andExpect(status().isMethodNotAllowed());
    }
}
