package com.pos.supplier.service;

import com.pos.common.exception.BusinessException;
import com.pos.common.exception.ResourceNotFoundException;
import com.pos.inventory.dto.AdjustmentRequest;
import com.pos.inventory.enums.AdjustmentType;
import com.pos.inventory.service.InventoryService;
import com.pos.product.repository.ProductRepository;
import com.pos.supplier.dto.CreatePurchaseOrderRequest;
import com.pos.supplier.dto.PurchaseOrderDto;
import com.pos.supplier.entity.PurchaseOrder;
import com.pos.supplier.entity.PurchaseOrderItem;
import com.pos.supplier.enums.PurchaseOrderStatus;
import com.pos.supplier.repository.PurchaseOrderRepository;
import com.pos.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public List<PurchaseOrderDto> findAll() {
        return purchaseOrderRepository.findAll().stream().map(this::toDto).toList();
    }

    public PurchaseOrderDto findById(UUID id) {
        return toDto(purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id)));
    }

    @Transactional
    public PurchaseOrderDto create(CreatePurchaseOrderRequest request, UUID userId) {
        supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", request.getSupplierId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplierId(request.getSupplierId());
        po.setNote(request.getNote());
        po.setCreatedBy(userId);

        for (CreatePurchaseOrderRequest.Item item : request.getItems()) {
            productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProductId()));
            PurchaseOrderItem poItem = new PurchaseOrderItem();
            poItem.setPurchaseOrder(po);
            poItem.setProductId(item.getProductId());
            poItem.setQtyOrdered(item.getQtyOrdered());
            poItem.setUnitCost(item.getUnitCost() != null ? item.getUnitCost() : BigDecimal.ZERO);
            poItem.setLineTotal(poItem.getUnitCost().multiply(BigDecimal.valueOf(item.getQtyOrdered())));
            po.getItems().add(poItem);
        }

        po.setTotal(po.getItems().stream()
                .map(PurchaseOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        return toDto(purchaseOrderRepository.save(po));
    }

    @Transactional
    public PurchaseOrderDto receiveGoods(UUID id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", id));
        if (po.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new BusinessException("PurchaseOrder is not pending");
        }
        for (PurchaseOrderItem item : po.getItems()) {
            AdjustmentRequest adj = new AdjustmentRequest();
            adj.setType(AdjustmentType.IN);
            adj.setQty(item.getQtyOrdered());
            adj.setNote("Goods received from PO " + po.getId());
            inventoryService.adjust(item.getProductId(), adj, po.getCreatedBy());
            item.setQtyReceived(item.getQtyOrdered());
        }
        po.setStatus(PurchaseOrderStatus.RECEIVED);
        return toDto(purchaseOrderRepository.save(po));
    }

    private PurchaseOrderDto toDto(PurchaseOrder po) {
        PurchaseOrderDto dto = new PurchaseOrderDto();
        dto.setId(po.getId());
        dto.setSupplierId(po.getSupplierId());
        dto.setStatus(po.getStatus());
        dto.setTotal(po.getTotal());
        dto.setNote(po.getNote());
        dto.setCreatedAt(po.getCreatedAt());
        supplierRepository.findById(po.getSupplierId())
                .ifPresent(s -> dto.setSupplierName(s.getName()));

        dto.setItems(po.getItems().stream().map(item -> {
            PurchaseOrderDto.PurchaseOrderItemDto d = new PurchaseOrderDto.PurchaseOrderItemDto();
            d.setId(item.getId());
            d.setProductId(item.getProductId());
            d.setQtyOrdered(item.getQtyOrdered());
            d.setQtyReceived(item.getQtyReceived());
            d.setUnitCost(item.getUnitCost());
            d.setLineTotal(item.getLineTotal());
            productRepository.findById(item.getProductId())
                    .ifPresent(p -> d.setProductName(p.getName()));
            return d;
        }).toList());
        return dto;
    }
}
