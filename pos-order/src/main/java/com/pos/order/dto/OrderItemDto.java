package com.pos.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class OrderItemDto {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private Integer qty;
    private BigDecimal unitPrice;
    private BigDecimal discount;
    private BigDecimal lineTotal;
}
