package com.pos.order.service;

import com.pos.common.exception.BusinessException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.customer.repository.CustomerRepository;
import com.pos.inventory.dto.AdjustmentRequest;
import com.pos.inventory.service.InventoryService;
import com.pos.order.dto.*;
import com.pos.order.entity.Order;
import com.pos.order.entity.OrderItem;
import com.pos.order.enums.OrderStatus;
import com.pos.order.repository.OrderRepository;
import com.pos.payment.dto.PaymentDto;
import com.pos.payment.enums.PaymentMethod;
import com.pos.payment.service.PaymentService;
import com.pos.product.entity.Product;
import com.pos.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    @Transactional
    public OrderDto openOrder(UUID cashierId, UUID customerId, String note) {
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setCashierId(cashierId);
        order.setCustomerId(customerId);
        order.setNote(note);
        return toDto(orderRepository.save(order));
    }

    public OrderDto findById(UUID id) {
        return toDto(getOrder(id));
    }

    @Transactional
    public OrderDto addItem(UUID orderId, AddItemRequest request) {
        Order order = getOrder(orderId);
        requireOpen(order);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        // Reserve stock
        inventoryService.reserveStock(request.getProductId(), request.getQty());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductId(product.getId());
        item.setQty(request.getQty());
        item.setUnitPrice(product.getPrice());
        item.setDiscount(request.getItemDiscount() != null ? request.getItemDiscount() : BigDecimal.ZERO);
        item.computeLineTotal();

        order.getItems().add(item);
        order.recalculate();
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto removeItem(UUID orderId, UUID itemId) {
        Order order = getOrder(orderId);
        requireOpen(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item", itemId));

        inventoryService.releaseReservation(item.getProductId(), item.getQty());
        order.getItems().remove(item);
        order.recalculate();
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto applyDiscount(UUID orderId, DiscountRequest request) {
        Order order = getOrder(orderId);
        requireOpen(order);
        order.setDiscount(request.getAmount());
        order.recalculate();
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public ReceiptDto pay(UUID orderId, PayRequest request) {
        Order order = getOrder(orderId);
        requireOpen(order);

        if (order.getItems().isEmpty()) {
            throw new BusinessException("Cannot pay an empty order");
        }
        if (request.getAmount().compareTo(order.getTotal()) < 0) {
            throw new BusinessException("Payment amount is less than order total");
        }

        // Deduct stock for all items
        order.getItems().forEach(item ->
                inventoryService.deductStock(item.getProductId(), item.getQty()));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        PaymentDto payment = paymentService.recordPayment(
                orderId, request.getMethod(), request.getAmount(), request.getRefNo());

        return buildReceipt(order, List.of(payment), request.getAmount());
    }

    @Transactional
    public OrderDto voidOrder(UUID orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessException("Only OPEN orders can be voided");
        }
        order.getItems().forEach(item ->
                inventoryService.releaseReservation(item.getProductId(), item.getQty()));
        order.setStatus(OrderStatus.VOIDED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto refundOrder(UUID orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Only PAID orders can be refunded");
        }
        order.getItems().forEach(item -> {
            AdjustmentRequest adj = new AdjustmentRequest();
            adj.setType(com.pos.inventory.enums.AdjustmentType.IN);
            adj.setQty(item.getQty());
            adj.setNote("Refund for order " + order.getOrderNo());
            inventoryService.adjust(item.getProductId(), adj, null);
        });
        order.setStatus(OrderStatus.REFUNDED);
        return toDto(orderRepository.save(order));
    }

    public ReceiptDto getReceipt(UUID orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Receipt only available for PAID orders");
        }
        List<PaymentDto> payments = paymentService.findByOrderId(orderId);
        BigDecimal totalPaid = payments.stream()
                .map(PaymentDto::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return buildReceipt(order, payments, totalPaid);
    }

    public List<OrderDto> findByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerId(customerId,
                org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream().map(this::toDto).toList();
    }

    private Order getOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private void requireOpen(Order order) {
        if (order.getStatus() != OrderStatus.OPEN) {
            throw new BusinessException("Order is not open (status: " + order.getStatus() + ")",
                    HttpStatus.CONFLICT);
        }
    }

    private ReceiptDto buildReceipt(Order order, List<PaymentDto> payments, BigDecimal amountPaid) {
        ReceiptDto receipt = new ReceiptDto();
        receipt.setOrderNo(order.getOrderNo());
        receipt.setPaidAt(LocalDateTime.now());
        receipt.setItems(order.getItems().stream().map(this::toItemDto).toList());
        receipt.setSubtotal(order.getSubtotal());
        receipt.setDiscount(order.getDiscount());
        receipt.setTax(order.getTax());
        receipt.setTotal(order.getTotal());
        receipt.setPayments(payments);
        receipt.setChange(amountPaid.subtract(order.getTotal()).max(BigDecimal.ZERO));

        if (order.getCustomerId() != null) {
            customerRepository.findById(order.getCustomerId())
                    .ifPresent(c -> receipt.setCustomerName(c.getName()));
        }
        return receipt;
    }

    private String generateOrderNo() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%05d", SEQ.incrementAndGet() % 100000);
    }

    private OrderDto toDto(Order o) {
        OrderDto dto = new OrderDto();
        dto.setId(o.getId());
        dto.setOrderNo(o.getOrderNo());
        dto.setCustomerId(o.getCustomerId());
        dto.setCashierId(o.getCashierId());
        dto.setStatus(o.getStatus());
        dto.setSubtotal(o.getSubtotal());
        dto.setDiscount(o.getDiscount());
        dto.setTax(o.getTax());
        dto.setTotal(o.getTotal());
        dto.setNote(o.getNote());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setItems(o.getItems().stream().map(this::toItemDto).toList());

        if (o.getCustomerId() != null) {
            customerRepository.findById(o.getCustomerId())
                    .ifPresent(c -> dto.setCustomerName(c.getName()));
        }
        return dto;
    }

    private OrderItemDto toItemDto(OrderItem i) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(i.getId());
        dto.setProductId(i.getProductId());
        dto.setQty(i.getQty());
        dto.setUnitPrice(i.getUnitPrice());
        dto.setDiscount(i.getDiscount());
        dto.setLineTotal(i.getLineTotal());
        productRepository.findById(i.getProductId()).ifPresent(p -> {
            dto.setProductName(p.getName());
            dto.setProductSku(p.getSku());
        });
        return dto;
    }
}
