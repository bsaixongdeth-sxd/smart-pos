package com.pos.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class SalesReportDto {

    private LocalDate from;
    private LocalDate to;
    private long totalOrders;
    private long paidOrders;
    private long voidedOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
}
