package com.pos.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class InventoryDto {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private int qtyOnHand;
    private int qtyReserved;
    private int availableQty;
    private int lowStockThreshold;
    private boolean lowStock;
    private LocalDateTime updatedAt;
}
