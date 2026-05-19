package com.pos.supplier.dto;

import com.pos.supplier.enums.PurchaseOrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PurchaseOrderDto {

    private UUID id;
    private UUID supplierId;
    private String supplierName;
    private PurchaseOrderStatus status;
    private BigDecimal total;
    private String note;
    private LocalDateTime createdAt;
    private List<PurchaseOrderItemDto> items;

    @Getter
    @Setter
    public static class PurchaseOrderItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private Integer qtyOrdered;
        private Integer qtyReceived;
        private BigDecimal unitCost;
        private BigDecimal lineTotal;
    }
}
