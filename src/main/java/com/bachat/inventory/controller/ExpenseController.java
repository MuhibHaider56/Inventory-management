package com.bachat.inventory.controller;

import com.bachat.inventory.dto.ExpenseCreateRequest;
import com.bachat.inventory.dto.ExpenseResponse;
import com.bachat.inventory.dto.ExpenseUpdateRequest;
import com.bachat.inventory.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@Tag(name = "Expenses", description = "Track business expenses for net profit/loss reporting.")
@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Operation(summary = "Create expense")
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(req));
    }

    @Operation(summary = "Get expense by id")
    @GetMapping("/{id}")
    public ExpenseResponse get(@PathVariable Long id) {
        return expenseService.get(id);
    }

    @Operation(summary = "List expenses (paginated, optional date filter)")
    @GetMapping
    public Page<ExpenseResponse> list(
            @Parameter(description = "Start date (inclusive)") @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate start,
            @Parameter(description = "End date (inclusive)") @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return expenseService.list(start, end, PageRequest.of(page, size));
    }

    @Operation(summary = "Update expense")
    @PutMapping("/{id}")
    public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseUpdateRequest req) {
        return expenseService.update(id, req);
    }

    @Operation(summary = "Delete expense")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
