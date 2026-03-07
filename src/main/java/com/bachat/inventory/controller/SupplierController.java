package com.bachat.inventory.controller;

import com.bachat.inventory.dto.SupplierRequest;
import com.bachat.inventory.dto.SupplierResponse;
import com.bachat.inventory.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Suppliers", description = "Manage suppliers you purchase stock from.")
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @Operation(summary = "Create supplier")
    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody SupplierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(req));
    }

    @Operation(summary = "Get supplier by id")
    @GetMapping("/{id}")
    public SupplierResponse get(@PathVariable Long id) {
        return supplierService.get(id);
    }

    @Operation(summary = "List suppliers (paginated)")
    @GetMapping
    public Page<SupplierResponse> list(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "20") int size) {
        return supplierService.list(PageRequest.of(page, size));
    }

    @Operation(summary = "Search suppliers by name or phone")
    @GetMapping("/search")
    public Page<SupplierResponse> search(@RequestParam String q,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return supplierService.search(q, PageRequest.of(page, size));
    }

    @Operation(summary = "Update supplier")
    @PutMapping("/{id}")
    public SupplierResponse update(@PathVariable Long id, @Valid @RequestBody SupplierRequest req) {
        return supplierService.update(id, req);
    }

    @Operation(summary = "Delete supplier (soft-delete if has purchase history)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
