package com.bachat.inventory.service;

import com.bachat.inventory.domain.Inventory;
import com.bachat.inventory.domain.Product;
import com.bachat.inventory.dto.ProductCreateRequest;
import com.bachat.inventory.dto.ProductResponse;
import com.bachat.inventory.dto.ProductUpdateRequest;
import com.bachat.inventory.exception.ConflictException;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.InventoryRepository;
import com.bachat.inventory.repository.OrderItemRepository;
import com.bachat.inventory.repository.ProductRepository;
import com.bachat.inventory.util.MoneyUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository,
                          InventoryRepository inventoryRepository,
                          OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        Product p = new Product();
        p.setName(req.getName().trim());
        p.setUnit(req.getUnit().trim());
        p.setCostPrice(MoneyUtil.scale2(req.getCostPrice()));
        p.setSellingPrice(MoneyUtil.scale2(req.getSellingPrice()));

        Product saved = productRepository.save(p);

        BigDecimal initialStock = req.getInitialStock() == null ? BigDecimal.ZERO : req.getInitialStock();
        Inventory inv = new Inventory(saved, MoneyUtil.scale2(initialStock));
        inventoryRepository.save(inv);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + id));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + id));

        p.setName(req.getName().trim());
        p.setUnit(req.getUnit().trim());
        p.setCostPrice(MoneyUtil.scale2(req.getCostPrice()));
        p.setSellingPrice(MoneyUtil.scale2(req.getSellingPrice()));

        return toResponse(productRepository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: id=" + id));

        boolean usedInOrders = orderItemRepository.existsByProduct_Id(id);
        if (usedInOrders) {
            throw new ConflictException("Cannot delete product because it exists in order history. Consider keeping it, or implement soft-delete.");
        }

        productRepository.delete(p);
        // inventory is removed by FK cascade if configured; if not, you can delete manually.
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getUnit(),
                p.getCostPrice(),
                p.getSellingPrice(),
                p.getCreatedAt()
        );
    }
}
