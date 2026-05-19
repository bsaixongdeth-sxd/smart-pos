package com.pos.product.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.product.dto.CategoryDto;
import com.pos.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Categories")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "List all categories")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.findAll()));
    }

    @Operation(summary = "Get category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDto>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.findById(id)));
    }

    @Operation(summary = "Create category (MANAGER+)")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDto>> create(@RequestBody CategoryDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", categoryService.create(request)));
    }
}
