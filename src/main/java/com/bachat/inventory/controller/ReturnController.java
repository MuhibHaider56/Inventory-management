package com.bachat.inventory.controller;

import com.bachat.inventory.dto.ReturnCreateRequest;
import com.bachat.inventory.dto.ReturnResponse;
import com.bachat.inventory.service.SalesReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Returns", description = "Process returns and refunds against orders.")
@RestController
@RequestMapping("/api/v1/returns")
public class ReturnController {

    private final SalesReturnService returnService;

    public ReturnController(SalesReturnService returnService) {
        this.returnService = returnService;
    }

    @Operation(summary = "Create a return against an order",
               description = "Returns specified items, optionally restocks inventory, calculates refund amount, and adjusts order totals.")
    @PostMapping
    public ResponseEntity<ReturnResponse> create(@Valid @RequestBody ReturnCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(returnService.createReturn(req));
    }

    @Operation(summary = "Mark a return as refunded",
               description = "Updates the refund status and reduces the order's amountPaid.")
    @PostMapping("/{id}/refund")
    public ReturnResponse markRefunded(@PathVariable Long id) {
        return returnService.markRefunded(id);
    }

    @Operation(summary = "Get returns for a specific order")
    @GetMapping("/order/{orderId}")
    public List<ReturnResponse> getByOrder(@PathVariable Long orderId) {
        return returnService.getByOrder(orderId);
    }

    @Operation(summary = "List all returns (paginated)")
    @GetMapping
    public Page<ReturnResponse> listAll(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return returnService.listAll(PageRequest.of(page, size));
    }
}
