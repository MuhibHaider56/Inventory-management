package com.bachat.inventory.controller;

import com.bachat.inventory.dto.PurchaseOrderCreateRequest;
import com.bachat.inventory.dto.PurchaseOrderResponse;
import com.bachat.inventory.service.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Purchase Orders", description = "Record stock purchases from suppliers. Auto-updates inventory and recalculates weighted average cost.")
@RestController
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    public PurchaseOrderController(PurchaseOrderService poService) {
        this.poService = poService;
    }

    @Operation(summary = "Create a purchase order",
               description = "Records a stock purchase from a supplier. Adds stock to inventory and recalculates the product's cost price using weighted average.")
    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(poService.create(req));
    }

    @Operation(summary = "Get purchase order by id")
    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return poService.get(id);
    }

    @Operation(summary = "List all purchase orders (paginated)")
    @GetMapping
    public Page<PurchaseOrderResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return poService.list(PageRequest.of(page, size, Sort.by("purchaseDate").descending()));
    }

    @Operation(summary = "List purchase orders by supplier")
    @GetMapping("/supplier/{supplierId}")
    public Page<PurchaseOrderResponse> listBySupplier(@PathVariable Long supplierId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return poService.listBySupplier(supplierId, PageRequest.of(page, size, Sort.by("purchaseDate").descending()));
    }

    @Operation(summary = "Cancel a purchase order",
               description = "Cancels the PO and reverses the inventory addition. Fails if stock would go below 0.")
    @PostMapping("/{id}/cancel")
    public PurchaseOrderResponse cancel(@PathVariable Long id) {
        return poService.cancel(id);
    }
}
