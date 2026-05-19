package com.pos.inventory.controller;

import com.pos.common.dto.ApiResponse;
import com.pos.inventory.dto.AdjustmentRequest;
import com.pos.inventory.dto.InventoryDto;
import com.pos.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Inventory")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get inventory by product ID")
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryDto>> get(@PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findByProductId(productId)));
    }

    @Operation(summary = "List low-stock items")
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryDto>>> lowStock() {
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.findLowStock()));
    }

    @Operation(summary = "Manual stock adjustment (MANAGER+)")
    @PutMapping("/{productId}/adjust")
    public ResponseEntity<ApiResponse<InventoryDto>> adjust(
            @PathVariable UUID productId,
            @Valid @RequestBody AdjustmentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        // principal.getUsername() is used only for audit; actual UUID fetched in service via username lookup
        return ResponseEntity.ok(ApiResponse.ok(inventoryService.adjust(productId, request, null)));
    }
}
