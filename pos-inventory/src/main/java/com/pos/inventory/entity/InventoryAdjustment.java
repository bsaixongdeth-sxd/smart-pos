package com.pos.inventory.entity;

import com.pos.common.entity.BaseEntity;
import com.pos.inventory.enums.AdjustmentType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "inventory_adjustments")
public class InventoryAdjustment extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AdjustmentType type;

    @Column(nullable = false)
    private Integer qty;

    @Column(length = 500)
    private String note;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
