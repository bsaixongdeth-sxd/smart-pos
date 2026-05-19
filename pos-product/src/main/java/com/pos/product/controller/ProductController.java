package com.pos.product.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.common.dto.PageResponse;
import com.pos.product.dto.CreateProductRequest;
import com.pos.product.dto.ProductDto;
import com.pos.product.dto.UpdateProductRequest;
import com.pos.product.service.ProductService;
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

@Tag(name = "Products")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Search/list products")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.ok(productService.search(search, pageable)));
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findById(id)));
    }

    @Operation(summary = "Get product by barcode")
    @GetMapping("/barcode/{code}")
    public ResponseEntity<ApiResponse<ProductDto>> getByBarcode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findByBarcode(code)));
    }

    @Operation(summary = "Create product (MANAGER+)")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> create(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created", productService.create(request)));
    }

    @Operation(summary = "Update product (MANAGER+)")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> update(@PathVariable UUID id,
                                                          @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(productService.update(id, request)));
    }

    @Operation(summary = "Deactivate product (MANAGER+)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deactivated"));
    }
}
