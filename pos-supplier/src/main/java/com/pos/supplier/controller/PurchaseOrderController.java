package com.pos.supplier.controller;

import com.pos.auth.entity.User;
import com.pos.common.dto.ApiResponse;
import com.pos.supplier.dto.CreatePurchaseOrderRequest;
import com.pos.supplier.dto.PurchaseOrderDto;
import com.pos.supplier.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Purchase Orders")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(summary = "List all purchase orders")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PurchaseOrderDto>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(purchaseOrderService.findAll()));
    }

    @Operation(summary = "Get purchase order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseOrderService.findById(id)));
    }

    @Operation(summary = "Create purchase order")
    @PostMapping
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Purchase order created",
                        purchaseOrderService.create(request, user.getId())));
    }

    @Operation(summary = "Receive goods and update stock")
    @PutMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<PurchaseOrderDto>> receive(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseOrderService.receiveGoods(id)));
    }
}
