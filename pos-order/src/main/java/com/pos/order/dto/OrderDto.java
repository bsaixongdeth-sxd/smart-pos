package com.pos.order.dto;

import com.pos.order.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class OrderDto {

    private UUID id;
    private String orderNo;
    private UUID customerId;
    private String customerName;
    private UUID cashierId;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private String note;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;
}
