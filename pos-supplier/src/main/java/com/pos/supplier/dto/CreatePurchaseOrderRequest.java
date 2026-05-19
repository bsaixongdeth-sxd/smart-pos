package com.pos.supplier.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreatePurchaseOrderRequest {

    @NotNull(message = "Supplier ID is required")
    private UUID supplierId;

    private String note;

    @NotNull(message = "Items are required")
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {
        @NotNull
        private UUID productId;

        @NotNull
        @Min(1)
        private Integer qtyOrdered;

        private BigDecimal unitCost;
    }
}
