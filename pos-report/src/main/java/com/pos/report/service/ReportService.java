package com.pos.report.service;

import com.pos.inventory.repository.InventoryRepository;
import com.pos.order.entity.Order;
import com.pos.order.entity.OrderItem;
import com.pos.order.enums.OrderStatus;
import com.pos.order.repository.OrderRepository;
import com.pos.payment.entity.Payment;
import com.pos.payment.enums.PaymentMethod;
import com.pos.payment.repository.PaymentRepository;
import com.pos.product.repository.ProductRepository;
import com.pos.report.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public SalesReportDto salesReport(LocalDate from, LocalDate to) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(
                from.atStartOfDay(), to.atTime(LocalTime.MAX));

        long paid = orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count();
        long voided = orders.stream().filter(o -> o.getStatus() == OrderStatus.VOIDED).count();
        BigDecimal revenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .map(Order::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .map(Order::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new SalesReportDto(from, to, orders.size(), paid, voided, revenue, discount, tax);
    }

    public DailyCloseDto dailyClose(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(
                OrderStatus.PAID, start, end);

        Set<UUID> orderIds = orders.stream().map(Order::getId).collect(Collectors.toSet());
        List<Payment> payments = orderIds.stream()
                .flatMap(id -> paymentRepository.findByOrderId(id).stream())
                .toList();

        BigDecimal cash = sumByMethod(payments, PaymentMethod.CASH);
        BigDecimal card = sumByMethod(payments, PaymentMethod.CARD);
        BigDecimal qr = sumByMethod(payments, PaymentMethod.QR);
        BigDecimal total = orders.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = orders.stream().map(Order::getDiscount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = orders.stream().map(Order::getTax).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DailyCloseDto(date, orders.size(), cash, card, qr, total, discount, tax);
    }

    public List<TopProductDto> topProducts(int limit) {
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Order> orders = orderRepository.findByStatusAndCreatedAtBetween(
                OrderStatus.PAID, monthStart, LocalDateTime.now());

        Map<UUID, long[]> stats = new HashMap<>(); // [qty, revenue*100 for precision]
        Map<UUID, BigDecimal> revenueMap = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                stats.computeIfAbsent(item.getProductId(), k -> new long[]{0});
                stats.get(item.getProductId())[0] += item.getQty();
                revenueMap.merge(item.getProductId(), item.getLineTotal(), BigDecimal::add);
            }
        }

        return stats.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]))
                .limit(limit)
                .map(e -> {
                    String name = productRepository.findById(e.getKey())
                            .map(p -> p.getName()).orElse("Unknown");
                    String sku = productRepository.findById(e.getKey())
                            .map(p -> p.getSku()).orElse("");
                    return new TopProductDto(e.getKey(), name, sku, e.getValue()[0],
                            revenueMap.getOrDefault(e.getKey(), BigDecimal.ZERO));
                })
                .toList();
    }

    public CashierSummaryDto cashierSummary(UUID cashierId, LocalDate date) {
        List<Order> orders = orderRepository.findByCashierIdAndCreatedAtBetween(
                cashierId, date.atStartOfDay(), date.atTime(LocalTime.MAX));
        BigDecimal revenue = orders.stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CashierSummaryDto(cashierId, date,
                orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID).count(), revenue);
    }

    public String exportCsv(LocalDate from, LocalDate to) {
        List<Order> orders = orderRepository.findByCreatedAtBetween(
                from.atStartOfDay(), to.atTime(LocalTime.MAX));
        StringBuilder sb = new StringBuilder("OrderNo,Status,Total,Discount,Tax,CreatedAt\n");
        for (Order o : orders) {
            sb.append(o.getOrderNo()).append(",")
                    .append(o.getStatus()).append(",")
                    .append(o.getTotal()).append(",")
                    .append(o.getDiscount()).append(",")
                    .append(o.getTax()).append(",")
                    .append(o.getCreatedAt()).append("\n");
        }
        return sb.toString();
    }

    private BigDecimal sumByMethod(List<Payment> payments, PaymentMethod method) {
        return payments.stream()
                .filter(p -> p.getMethod() == method)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
