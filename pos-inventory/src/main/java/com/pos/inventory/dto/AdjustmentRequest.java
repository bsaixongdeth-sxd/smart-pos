package com.pos.inventory.dto;

import com.pos.inventory.enums.AdjustmentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdjustmentRequest {

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType type;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer qty;

    private String note;
}
