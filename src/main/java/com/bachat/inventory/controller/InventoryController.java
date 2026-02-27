package com.bachat.inventory.controller;

import com.bachat.inventory.dto.InventoryAdjustRequest;
import com.bachat.inventory.dto.InventoryItemResponse;
import com.bachat.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Inventory", description = "Manage stock quantities for products.")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Operation(summary = "List inventory", description = "Returns current stock for all products.")
    @GetMapping
    public List<InventoryItemResponse> list() {
        return inventoryService.list();
    }

    @Operation(summary = "Get inventory by product id", description = "Returns current stock for a single product.")
    @GetMapping("/{productId}")
    public InventoryItemResponse get(@PathVariable Long productId) {
        return inventoryService.getByProductId(productId);
    }

    @Operation(
            summary = "Adjust stock (increase/decrease)",
            description = "Adjusts inventory by a delta quantity. Use a positive value to add stock and a negative value to remove stock."
    )
    @PostMapping("/adjust")
    public ResponseEntity<InventoryItemResponse> adjust(@Valid @RequestBody InventoryAdjustRequest req) {
        InventoryItemResponse res = inventoryService.adjust(req);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @Operation(summary = "Low stock list", description = "Returns products whose quantity is less than or equal to the threshold.")
    @GetMapping("/low-stock")
    public List<InventoryItemResponse> lowStock(
            @Parameter(description = "Low stock threshold (default: 10).", example = "10")
            @RequestParam(defaultValue = "10") BigDecimal threshold
    ) {
        return inventoryService.lowStock(threshold);
    }
}
