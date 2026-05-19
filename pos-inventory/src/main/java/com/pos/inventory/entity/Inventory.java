package com.pos.inventory.entity;

import com.pos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    @Column(name = "product_id", unique = true, nullable = false)
    private UUID productId;

    @Column(name = "qty_on_hand", nullable = false)
    private Integer qtyOnHand = 0;

    @Column(name = "qty_reserved", nullable = false)
    private Integer qtyReserved = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 10;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public int getAvailableQty() {
        return qtyOnHand - qtyReserved;
    }

    public boolean isLowStock() {
        return qtyOnHand <= lowStockThreshold;
    }
}
