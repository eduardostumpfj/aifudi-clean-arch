package com.aifudi_clean_arch.backend.infra.web.handler;

import com.aifudi_clean_arch.backend.core.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException error, HttpServletRequest request) {
        var problem = createProblem(HttpStatus.UNAUTHORIZED, "Authentication failed", error.getMessage(), request);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"aifudi\", charset=\"UTF-8\"")
                .body(problem);
    }

    @ExceptionHandler(NotAllowedException.class)
    public ProblemDetail handleNotAllowed(NotAllowedException error, HttpServletRequest request) {
        return createProblem(HttpStatus.FORBIDDEN, "Not allowed", error.getMessage(), request);
    }

    @ExceptionHandler({UserNotFoundException.class, RestaurantNotFoundException.class,
            KitchenNotFoundException.class, RoleNotFoundException.class,
            SubRoleNotFoundException.class, MenuItemNotFountException.class})
    public ProblemDetail handleNotFound(RuntimeException error, HttpServletRequest request) {
        return createProblem(HttpStatus.NOT_FOUND, "Resource not found", error.getMessage(), request);
    }

    @ExceptionHandler({MenuItemInvalidPrice.class, IllegalArgumentException.class})
    public ProblemDetail handleInvalidInput(RuntimeException error, HttpServletRequest request) {
        return createProblem(HttpStatus.BAD_REQUEST, "Invalid input", error.getMessage(), request);
    }

    @ExceptionHandler(ProtectedSubRoleException.class)
    public ProblemDetail handleProtectedSubRole(ProtectedSubRoleException error, HttpServletRequest request) {
        return createProblem(HttpStatus.FORBIDDEN, "Protected subrole", error.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConflict(DataIntegrityViolationException error, HttpServletRequest request) {
        return createProblem(HttpStatus.CONFLICT, "Resource conflict",
                "The operation conflicts with existing data or related resources.", request);
    }

    @ExceptionHandler(ErrorOnRepositoryAccess.class)
    public ProblemDetail handleRepositoryError(ErrorOnRepositoryAccess error, HttpServletRequest request) {
        // Gateways wrap persistence errors; preserve conflicts without exposing SQL details.
        for (Throwable cause = error.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof DataIntegrityViolationException conflict) {
                return handleConflict(conflict, request);
            }
        }
        return handleUnexpectedError(error, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException error,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid.");
        problem.setTitle("Validation failed");
        var errors = new ArrayList<String>();
        error.getBindingResult().getFieldErrors().forEach(field ->
                errors.add(field.getField() + ": " + field.getDefaultMessage()));
        error.getBindingResult().getGlobalErrors().forEach(global -> errors.add(global.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return handleExceptionInternal(error, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException error,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "The request body is missing, malformed or contains a value of an invalid type.");
        problem.setTitle("Invalid request body");
        return handleExceptionInternal(error, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedError(Exception error, HttpServletRequest request) {
        log.error("Unexpected error handling {} {}", request.getMethod(), request.getRequestURI(), error);
        return createProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
                "An unexpected error occurred. Please try again later.", request);
    }

    private ProblemDetail createProblem(HttpStatus status, String title, String detail,
                                        HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
