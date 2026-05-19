package com.pos.inventory.service;

import com.pos.common.exception.BusinessException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.inventory.dto.AdjustmentRequest;
import com.pos.inventory.dto.InventoryDto;
import com.pos.inventory.entity.Inventory;
import com.pos.inventory.entity.InventoryAdjustment;
import com.pos.inventory.enums.AdjustmentType;
import com.pos.inventory.repository.InventoryAdjustmentRepository;
import com.pos.inventory.repository.InventoryRepository;
import com.pos.product.entity.Product;
import com.pos.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final ProductRepository productRepository;

    public InventoryDto findByProductId(UUID productId) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", productId));
        return toDto(inv);
    }

    public List<InventoryDto> findLowStock() {
        return inventoryRepository.findLowStock().stream().map(this::toDto).toList();
    }

    @Transactional
    public InventoryDto adjust(UUID productId, AdjustmentRequest request, UUID userId) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseGet(() -> createInventory(productId));

        int delta = request.getQty();
        switch (request.getType()) {
            case IN -> inv.setQtyOnHand(inv.getQtyOnHand() + delta);
            case OUT -> {
                if (inv.getQtyOnHand() < delta) {
                    throw new BusinessException("Insufficient stock. Available: " + inv.getQtyOnHand());
                }
                inv.setQtyOnHand(inv.getQtyOnHand() - delta);
            }
            case ADJUST -> inv.setQtyOnHand(delta);
        }
        inventoryRepository.save(inv);

        InventoryAdjustment adj = new InventoryAdjustment();
        adj.setProductId(productId);
        adj.setType(request.getType());
        adj.setQty(request.getQty());
        adj.setNote(request.getNote());
        adj.setCreatedBy(userId);
        adjustmentRepository.save(adj);

        if (inv.isLowStock()) {
            log.warn("Low stock alert: product={}, qty={}", productId, inv.getQtyOnHand());
        }
        return toDto(inv);
    }

    @Transactional
    public void reserveStock(UUID productId, int qty) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", productId));
        if (inv.getAvailableQty() < qty) {
            throw new BusinessException("Insufficient available stock for product: " + productId);
        }
        inv.setQtyReserved(inv.getQtyReserved() + qty);
        inventoryRepository.save(inv);
    }

    @Transactional
    public void releaseReservation(UUID productId, int qty) {
        inventoryRepository.findByProductId(productId).ifPresent(inv -> {
            inv.setQtyReserved(Math.max(0, inv.getQtyReserved() - qty));
            inventoryRepository.save(inv);
        });
    }

    @Transactional
    public void deductStock(UUID productId, int qty) {
        Inventory inv = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory for product", productId));
        inv.setQtyOnHand(inv.getQtyOnHand() - qty);
        inv.setQtyReserved(Math.max(0, inv.getQtyReserved() - qty));
        inventoryRepository.save(inv);
    }

    private Inventory createInventory(UUID productId) {
        Inventory inv = new Inventory();
        inv.setProductId(productId);
        return inventoryRepository.save(inv);
    }

    private InventoryDto toDto(Inventory inv) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inv.getId());
        dto.setProductId(inv.getProductId());
        dto.setQtyOnHand(inv.getQtyOnHand());
        dto.setQtyReserved(inv.getQtyReserved());
        dto.setAvailableQty(inv.getAvailableQty());
        dto.setLowStockThreshold(inv.getLowStockThreshold());
        dto.setLowStock(inv.isLowStock());
        dto.setUpdatedAt(inv.getUpdatedAt());
        productRepository.findById(inv.getProductId()).ifPresent(p -> {
            dto.setProductName(p.getName());
            dto.setProductSku(p.getSku());
        });
        return dto;
    }
}
