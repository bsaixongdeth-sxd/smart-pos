package com.pos.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CashierSummaryDto {

    private UUID cashierId;
    private LocalDate date;
    private long totalOrders;
    private BigDecimal totalRevenue;
}
