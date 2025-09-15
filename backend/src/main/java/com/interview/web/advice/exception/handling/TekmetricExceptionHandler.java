package com.interview.web.advice.exception.handling;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.interview.web.advice.exception.handling.ProblemType.CLIENT_ERROR;
import static com.interview.web.advice.exception.handling.ProblemType.SERVER_INTERNAL_ERROR;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class TekmetricExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fields =
                ex
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> ofNullable(fe.getDefaultMessage()).orElse("Invalid value"),
                        (a,b)->a, LinkedHashMap::new));

        ProblemDetail pd = problem(BAD_REQUEST, "Validation failed", req);
        pd.setProperty("fields", fields);
        pd.setType(CLIENT_ERROR.getUri());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String,String> violations = ex
                .getConstraintViolations()
                .stream()
                .collect(Collectors
                        .toMap(
                                v -> v.getPropertyPath().toString(),
                                v -> v.getMessage(),
                                (a,b)->a, LinkedHashMap::new));

        ProblemDetail pd = problem(BAD_REQUEST, "Constraint violation", req);
        pd.setProperty("violations", violations);
        pd.setType(CLIENT_ERROR.getUri());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(pd);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(BAD_REQUEST, "Malformed JSON request", req);
        pd.setType(CLIENT_ERROR.getUri());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(pd);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg =
                "Parameter '%s' must be of type %s".formatted(
                        ex.getName(),
                        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required");
        ProblemDetail pd = problem(BAD_REQUEST, msg, req);
        pd.setType(CLIENT_ERROR.getUri());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(pd);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(BAD_REQUEST, "Missing request parameter: " + ex.getParameterName(), req);
        pd.setType(CLIENT_ERROR.getUri());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.CONFLICT, "Constraint violation", req);
        pd.setType(SERVER_INTERNAL_ERROR.getUri());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(pd);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimistic(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.CONFLICT, "Version conflict", req);
        pd.setType(CLIENT_ERROR.getUri());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(pd);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleRSE(ResponseStatusException ex, HttpServletRequest req) {
        HttpStatusCode sc = ex.getStatusCode();
        HttpStatus status = HttpStatus.valueOf(sc.value());
        ProblemDetail pd = problem(
                status,
                ofNullable(ex.getReason()).orElse("Error"),
                req);
        URI uri = status.value() < HttpStatus.INTERNAL_SERVER_ERROR.value() ?
                CLIENT_ERROR.getUri() :
                SERVER_INTERNAL_ERROR.getUri();
        pd.setType(uri);
        return ResponseEntity
                .status(sc)
                .body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleOther(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req);
        pd.setType(SERVER_INTERNAL_ERROR.getUri());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }

    private ProblemDetail problem(HttpStatus status, String message, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setTitle(status.getReasonPhrase());
        pd.setProperty("path", req.getRequestURI());
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        return pd;
    }
}
