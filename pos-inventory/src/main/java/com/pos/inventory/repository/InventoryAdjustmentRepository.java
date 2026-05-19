package com.pos.inventory.repository;

import com.pos.inventory.entity.InventoryAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, UUID> {

    List<InventoryAdjustment> findByProductIdOrderByCreatedAtDesc(UUID productId);
}
