package com.bachat.inventory.controller;

import com.bachat.inventory.dto.ProductCreateRequest;
import com.bachat.inventory.dto.ProductResponse;
import com.bachat.inventory.dto.ProductUpdateRequest;
import com.bachat.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "Manage product catalog (wheat, rice, flour, etc.) and pricing.")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
            summary = "Create a product",
            description = "Creates a new product with unit, cost price and selling price. You may also set an initial stock quantity."
    )
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        ProductResponse created = productService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get product by id")
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }

    @Operation(summary = "List products (paginated)")
    @GetMapping
    public Page<ProductResponse> list(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.list(pageable);
    }

    @Operation(
            summary = "Update a product",
            description = "Updates product details such as name/unit and prices. Note: existing orders keep their own locked-in prices."
    )
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest req) {
        return productService.update(id, req);
    }

    @Operation(
            summary = "Delete a product",
            description = "Deletes a product. If your business rules prevent deleting products with historical order items, the API will reject it."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
