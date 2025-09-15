package com.interview.web.controller;

import com.interview.service.api.CarsService;
import com.interview.validation.groups.*;
import com.interview.web.annotation.LocationHeaderOnPost;
import com.interview.web.dto.cars.CarsDto;
import com.interview.web.dto.cars.CarsRequest;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
@Tag(name = "Cars", description = "CRUD for Cars")
@Validated
public class CarsController {

    private final CarsService service;

    @Operation(
            summary = "List all cars",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieve entries"),
                    @ApiResponse(responseCode = "401", description = "Request not authorized"),
                    @ApiResponse(responseCode = "403", description = "Request forbidden"),
                    @ApiResponse(responseCode = "404", description = "Resource not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping
    @CircuitBreaker(name = "carsApi", fallbackMethod = "listFallback")
    @Retry(name = "carsApi")
    @RateLimiter(name = "carsApi")
    @Bulkhead(name = "carsApi")
    public Page<CarsDto> list(Pageable pageable,
                              @RequestParam(required = false) String make,
                              @RequestParam(required = false) String model) {
        Page<CarsDto> list = service.list(pageable, make, model);
        return list;
    }

    @Operation(
            summary = "Get car by id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieve entry"),
                    @ApiResponse(responseCode = "401", description = "Request not authorized"),
                    @ApiResponse(responseCode = "403", description = "Request forbidden"),
                    @ApiResponse(responseCode = "404", description = "Resource not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/{id}")
    @CircuitBreaker(name = "carsApi", fallbackMethod = "getByIdFallback")
    @Retry(name = "carsApi")
    @RateLimiter(name = "carsApi")
    @Bulkhead(name = "carsApi")
    public CarsDto get(@PathVariable Long id) {
        return service.getById(id);
    }

    @Operation(
            summary = "Get car by VIN",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieve entry"),
                    @ApiResponse(responseCode = "401", description = "Request not authorized"),
                    @ApiResponse(responseCode = "403", description = "Request forbidden"),
                    @ApiResponse(responseCode = "404", description = "Resource not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @GetMapping("/by-vin/{vin}")
    @CircuitBreaker(name = "carsApi", fallbackMethod = "getByVinFallback")
    @Retry(name = "carsApi")
    @RateLimiter(name = "carsApi")
    @Bulkhead(name = "carsApi")
    public CarsDto getByVin(
            @PathVariable
            @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "VIN invalid")
            String vin) {
        return service.getByVin(vin);
    }

    @Operation(
            summary = "Create car",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Successfully creates entry"),
                    @ApiResponse(responseCode = "401", description = "Request not authorized"),
                    @ApiResponse(responseCode = "403", description = "Request forbidden"),
                    @ApiResponse(responseCode = "404", description = "Resource not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PostMapping
    @LocationHeaderOnPost
    @CircuitBreaker(name = "carsApi", fallbackMethod = "createFallback")
    @Retry(name = "carsApi")
    @RateLimiter(name = "carsApi")
    @Bulkhead(name = "carsApi")
    public ResponseEntity<CarsDto> create(@Validated(OnCreate.class) @RequestBody CarsRequest req) {
        CarsDto dto = service.create(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(dto);
    }

    @Operation(
            summary = "Replace car",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updates entry"),
                    @ApiResponse(responseCode = "401", description = "Request not authorized"),
                    @ApiResponse(responseCode = "403", description = "Request forbidden"),
                    @ApiResponse(responseCode = "404", description = "Resource not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PutMapping("/{id}")
    @CircuitBreaker(name = "carsApi", fallbackMethod = "replaceFallback")
    @Retry(name = "carsApi")
    @RateLimiter(name = "carsApi")
    @Bulkhead(name = "carsApi")
    public CarsDto replace(@PathVariable Long id,
                           @Validated(OnUpdate.class) @RequestBody CarsRequest req) {
        return service.replace(id, req);
    }

    @Operation(
            summary = "Partial car update",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully updated entry"),
                    @ApiResponse(responseCode = "401", description = "Request not authorized"),
                    @ApiResponse(responseCode = "403", description = "Request forbidden"),
                    @ApiResponse(responseCode = "404", description = "Resource not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @PatchMapping("/{id}")
    @CircuitBreaker(name = "carsApi", fallbackMethod = "patchFallback")
    @RateLimiter(name = "carsApi")
    @Bulkhead(name = "carsApi")
    public CarsDto patch(@PathVariable Long id,
                         @Validated(OnPatch.class) @RequestBody CarsRequest req) {
        return service.patch(id, req);
    }

    @Operation(
            summary = "Delete car",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deletes entry"),
                    @ApiResponse(responseCode = "401", description = "Request not authorized"),
                    @ApiResponse(responseCode = "403", description = "Request forbidden"),
                    @ApiResponse(responseCode = "404", description = "Resource not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")})
    @DeleteMapping("/{id}")
    @CircuitBreaker(name = "carsApi", fallbackMethod = "deleteFallback")
    @RateLimiter(name = "carsApi")
    @Bulkhead(name = "carsApi")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }

    public Page<CarsDto> listFallback(Pageable pageable, String make, String model, Throwable ex) {
        return new PageImpl<>(java.util.List.of(), pageable, 0);
    }

    public CarsDto getByIdFallback(Long id, Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cars temporarily unavailable");
    }

    public CarsDto getByVinFallback(String vin, Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Cars temporarily unavailable");
    }

    public ResponseEntity<CarsDto> createFallback(CarsRequest req, Throwable ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    public CarsDto replaceFallback(Long id, CarsRequest req, Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Replace unavailable");
    }

    public CarsDto patchFallback(Long id, CarsRequest req, Throwable ex) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Patch unavailable");
    }

    public ResponseEntity<Void> deleteFallback(Long id, Throwable ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
