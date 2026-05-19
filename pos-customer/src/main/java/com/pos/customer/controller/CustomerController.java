package com.pos.customer.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.customer.dto.CreateCustomerRequest;
import com.pos.customer.dto.CustomerDto;
import com.pos.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Customers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Search/list customers")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CustomerDto>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                customerService.search(search, PageRequest.of(page, size, Sort.by("name")))));
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.findById(id)));
    }

    @Operation(summary = "Create customer")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerDto>> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer created", customerService.create(request)));
    }

    @Operation(summary = "Update customer")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDto>> update(@PathVariable UUID id,
                                                           @RequestBody CreateCustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.update(id, request)));
    }
}
