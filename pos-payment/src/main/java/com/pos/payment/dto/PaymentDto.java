package com.pos.payment.dto;

import com.pos.payment.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class PaymentDto {

    private UUID id;
    private UUID orderId;
    private PaymentMethod method;
    private BigDecimal amount;
    private String refNo;
    private LocalDateTime paidAt;
}
