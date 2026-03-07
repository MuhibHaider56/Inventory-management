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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "Manage product catalog and pricing.")
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Create a product")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(req));
    }

    @Operation(summary = "Get product by id")
    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return productService.get(id);
    }

    @Operation(summary = "List products (paginated, excludes soft-deleted)")
    @GetMapping
    public Page<ProductResponse> list(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size) {
        return productService.list(PageRequest.of(page, size));
    }

    @Operation(summary = "Search products by name")
    @GetMapping("/search")
    public Page<ProductResponse> search(@RequestParam String q,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return productService.search(q, PageRequest.of(page, size));
    }

    @Operation(summary = "Update a product")
    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest req) {
        return productService.update(id, req);
    }

    @Operation(summary = "Soft-delete a product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
