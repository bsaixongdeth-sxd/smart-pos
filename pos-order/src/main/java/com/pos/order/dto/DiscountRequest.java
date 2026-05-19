package com.pos.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DiscountRequest {

    @NotNull(message = "Discount amount is required")
    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private BigDecimal amount;

    private String reason;
}
