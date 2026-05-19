package com.pos.supplier.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.supplier.dto.SupplierDto;
import com.pos.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Suppliers")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "List all suppliers")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(supplierService.findAll()));
    }

    @Operation(summary = "Get supplier by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDto>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(supplierService.findById(id)));
    }

    @Operation(summary = "Create supplier")
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierDto>> create(@Valid @RequestBody SupplierDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Supplier created", supplierService.create(request)));
    }
}
