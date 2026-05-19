package com.pos.supplier.entity;

import com.pos.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "purchase_order_items")
public class PurchaseOrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "qty_ordered", nullable = false)
    private Integer qtyOrdered;

    @Column(name = "qty_received", nullable = false)
    private Integer qtyReceived = 0;

    @Column(name = "unit_cost", precision = 18, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "line_total", precision = 18, scale = 2)
    private BigDecimal lineTotal;
}
