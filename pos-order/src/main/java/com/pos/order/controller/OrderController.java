package com.pos.order.controller;

import com.pos.auth.entity.User;
import com.pos.common.dto.ApiResponse;
import com.pos.order.dto.*;

import java.util.List;
import com.pos.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Open a new order")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> open(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal User cashier) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                orderService.openOrder(cashier.getId(), customerId, note)));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findById(id)));
    }

    @Operation(summary = "Add item to order")
    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<OrderDto>> addItem(@PathVariable UUID id,
                                                         @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.addItem(id, request)));
    }

    @Operation(summary = "Remove item from order")
    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<ApiResponse<OrderDto>> removeItem(@PathVariable UUID id,
                                                            @PathVariable UUID itemId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.removeItem(id, itemId)));
    }

    @Operation(summary = "Apply discount to order")
    @PostMapping("/{id}/discount")
    public ResponseEntity<ApiResponse<OrderDto>> applyDiscount(@PathVariable UUID id,
                                                               @Valid @RequestBody DiscountRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.applyDiscount(id, request)));
    }

    @Operation(summary = "Process payment")
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<ReceiptDto>> pay(@PathVariable UUID id,
                                                       @Valid @RequestBody PayRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.pay(id, request)));
    }

    @Operation(summary = "Void order (MANAGER+)")
    @PostMapping("/{id}/void")
    public ResponseEntity<ApiResponse<OrderDto>> voidOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.voidOrder(id)));
    }

    @Operation(summary = "Refund order (MANAGER+)")
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<OrderDto>> refund(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.refundOrder(id)));
    }

    @Operation(summary = "Get printable receipt")
    @GetMapping("/{id}/receipt")
    public ResponseEntity<ApiResponse<ReceiptDto>> receipt(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getReceipt(id)));
    }

    @Operation(summary = "List orders by customer")
    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<ApiResponse<List<OrderDto>>> byCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findByCustomerId(customerId)));
    }
}
