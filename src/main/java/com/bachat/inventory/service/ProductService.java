package com.bachat.inventory.service;

import com.bachat.inventory.domain.Inventory;
import com.bachat.inventory.domain.Product;
import com.bachat.inventory.dto.ProductCreateRequest;
import com.bachat.inventory.dto.ProductResponse;
import com.bachat.inventory.dto.ProductUpdateRequest;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.InventoryRepository;
import com.bachat.inventory.repository.ProductRepository;
import com.bachat.inventory.util.MoneyUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final AuditService auditService;

    public ProductService(ProductRepository productRepository,
                          InventoryRepository inventoryRepository,
                          AuditService auditService) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        Product p = new Product();
        p.setName(req.getName().trim());
        p.setUnit(req.getUnit().trim());
        p.setCostPrice(MoneyUtil.scale2(req.getCostPrice()));
        p.setSellingPrice(MoneyUtil.scale2(req.getSellingPrice()));

        BigDecimal initialStock = req.getInitialStock() == null ? BigDecimal.ZERO : MoneyUtil.scale2(req.getInitialStock());
        p.setInitialStock(initialStock);
        p.setInitialCostPrice(p.getCostPrice());

        Product saved = productRepository.save(p);

        Inventory inv = new Inventory(saved, initialStock);
        inventoryRepository.save(inv);

        auditService.log("PRODUCT", saved.getId(), "CREATE",
                "Product created: " + saved.getName() + ", initial stock=" + initialStock);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product p = findActiveById(id);
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Pageable pageable) {
        return productRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, Pageable pageable) {
        return productRepository.searchByName(query, pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest req) {
        Product p = findActiveById(id);

        String oldInfo = p.getName() + " | cost=" + p.getCostPrice() + " | sell=" + p.getSellingPrice();

        p.setName(req.getName().trim());
        p.setUnit(req.getUnit().trim());
        p.setCostPrice(MoneyUtil.scale2(req.getCostPrice()));
        p.setSellingPrice(MoneyUtil.scale2(req.getSellingPrice()));

        Product saved = productRepository.save(p);

        String newInfo = saved.getName() + " | cost=" + saved.getCostPrice() + " | sell=" + saved.getSellingPrice();
        auditService.log("PRODUCT", saved.getId(), "UPDATE",
                "Product updated", oldInfo, newInfo);

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Product p = findActiveById(id);

        // Clear corresponding inventory
        inventoryRepository.findByProduct_Id(id).ifPresent(inv -> {
            BigDecimal oldQty = inv.getQuantityAvailable();
            inv.setQuantityAvailable(BigDecimal.ZERO);
            inventoryRepository.save(inv);
            auditService.log("INVENTORY", id, "CLEARED",
                    "Inventory cleared on product deletion: " + p.getName(),
                    oldQty.toString(), "0");
        });

        p.setDeleted(true);
        p.setDeletedAt(LocalDateTime.now());
        p.setDeletedBy(auditService.getCurrentUsername());
        productRepository.save(p);

        auditService.log("PRODUCT", id, "SOFT_DELETE",
                "Product soft-deleted: " + p.getName());
    }

    private Product findActiveById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + id));
        if (p.isDeleted()) {
            throw new ResourceNotFoundException("Product has been deleted: id=" + id);
        }
        return p;
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getUnit(),
                p.getCostPrice(), p.getSellingPrice(), p.getCreatedAt());
    }
}
