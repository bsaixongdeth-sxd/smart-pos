package com.pos.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TopProductDto {

    private UUID productId;
    private String productName;
    private String sku;
    private long totalQtySold;
    private BigDecimal totalRevenue;
}
