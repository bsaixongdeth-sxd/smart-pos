package com.pos.report.scheduled;

import com.pos.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockAlertJob {

    private final InventoryRepository inventoryRepository;

    @Scheduled(cron = "0 0 8 * * *")
    public void checkLowStock() {
        var lowStockItems = inventoryRepository.findLowStock();
        if (!lowStockItems.isEmpty()) {
            log.warn("LOW STOCK ALERT: {} product(s) below threshold", lowStockItems.size());
            lowStockItems.forEach(inv ->
                    log.warn("  Product={}, onHand={}, threshold={}",
                            inv.getProductId(), inv.getQtyOnHand(), inv.getLowStockThreshold()));
        }
    }
}
