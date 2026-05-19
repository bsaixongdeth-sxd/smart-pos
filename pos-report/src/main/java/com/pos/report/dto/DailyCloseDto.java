package com.pos.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyCloseDto {

    private LocalDate date;
    private long totalOrders;
    private BigDecimal cashTotal;
    private BigDecimal cardTotal;
    private BigDecimal qrTotal;
    private BigDecimal grandTotal;
    private BigDecimal totalDiscount;
    private BigDecimal totalTax;
}
