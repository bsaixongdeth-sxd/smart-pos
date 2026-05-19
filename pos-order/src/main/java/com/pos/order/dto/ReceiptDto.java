package com.pos.order.dto;

import com.pos.payment.dto.PaymentDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReceiptDto {

    private String orderNo;
    private LocalDateTime paidAt;
    private String cashierName;
    private String customerName;
    private List<OrderItemDto> items;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private List<PaymentDto> payments;
    private BigDecimal change;
}
