package com.pos.payment.service;

import com.pos.payment.dto.PaymentDto;
import com.pos.payment.entity.Payment;
import com.pos.payment.enums.PaymentMethod;
import com.pos.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentDto recordPayment(UUID orderId, PaymentMethod method, BigDecimal amount, String refNo) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setMethod(method);
        payment.setAmount(amount);
        payment.setRefNo(refNo);
        return toDto(paymentRepository.save(payment));
    }

    public List<PaymentDto> findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream().map(this::toDto).toList();
    }

    private PaymentDto toDto(Payment p) {
        PaymentDto dto = new PaymentDto();
        dto.setId(p.getId());
        dto.setOrderId(p.getOrderId());
        dto.setMethod(p.getMethod());
        dto.setAmount(p.getAmount());
        dto.setRefNo(p.getRefNo());
        dto.setPaidAt(p.getPaidAt());
        return dto;
    }
}
