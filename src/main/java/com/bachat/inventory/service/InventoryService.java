package com.bachat.inventory.service;

import com.bachat.inventory.domain.Inventory;
import com.bachat.inventory.domain.Product;
import com.bachat.inventory.dto.InventoryAdjustRequest;
import com.bachat.inventory.dto.InventoryItemResponse;
import com.bachat.inventory.exception.BadRequestException;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.InventoryRepository;
import com.bachat.inventory.repository.ProductRepository;
import com.bachat.inventory.util.MoneyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            AuditService auditService) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> list() {
        return inventoryRepository.findAllWithProduct().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getByProductId(Long productId) {
        Inventory inv = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for productId=" + productId));
        return toResponse(inv);
    }

    @Transactional
    public InventoryItemResponse adjust(InventoryAdjustRequest req) {
        if (req.getDelta() == null) throw new BadRequestException("delta is required");
        if (req.getProductId() == null) throw new BadRequestException("productId is required");

        Long productId = req.getProductId();
        Inventory inv = inventoryRepository.findByProductIdForUpdate(productId).orElse(null);

        if (inv == null) {
            Product p = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + productId));
            inv = new Inventory(p, BigDecimal.ZERO);
        }

        BigDecimal oldQty = inv.getQuantityAvailable();
        BigDecimal delta = MoneyUtil.scale2(req.getDelta());
        BigDecimal newQty = MoneyUtil.add(inv.getQuantityAvailable(), delta);

        if (newQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Stock cannot go below 0. Current=" + oldQty + ", delta=" + delta);
        }

        inv.setQuantityAvailable(newQty);
        Inventory saved = inventoryRepository.save(inv);

        auditService.log("INVENTORY", productId, "ADJUST",
                "Stock adjusted by " + delta + " for product " + inv.getProduct().getName(),
                oldQty.toString(), newQty.toString());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> lowStock(BigDecimal threshold) {
        if (threshold == null) threshold = BigDecimal.ZERO;
        threshold = MoneyUtil.scale2(threshold);
        return inventoryRepository.findLowStockWithProduct(threshold)
                .stream().map(this::toResponse).toList();
    }

    private InventoryItemResponse toResponse(Inventory inv) {
        Product p = inv.getProduct();
        return new InventoryItemResponse(
                p.getId(), p.getName(), p.getUnit(),
                inv.getQuantityAvailable(), inv.getLastUpdated());
    }
}
