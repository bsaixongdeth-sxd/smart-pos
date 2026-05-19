package com.pos.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ProductDto {

    private UUID id;
    private String sku;
    private String barcode;
    private String name;
    private UUID categoryId;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
