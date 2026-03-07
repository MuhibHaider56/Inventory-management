package com.bachat.inventory.controller;

import com.bachat.inventory.dto.CustomerCreateRequest;
import com.bachat.inventory.dto.CustomerResponse;
import com.bachat.inventory.dto.CustomerUpdateRequest;
import com.bachat.inventory.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Customers", description = "Manage customers/clients who place orders.")
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create customer")
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(req));
    }

    @Operation(summary = "Get customer by id")
    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable Long id) {
        return customerService.get(id);
    }

    @Operation(summary = "List customers (paginated, excludes soft-deleted)")
    @GetMapping
    public Page<CustomerResponse> list(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        return customerService.list(PageRequest.of(page, size));
    }

    @Operation(summary = "Search customers by name or phone")
    @GetMapping("/search")
    public Page<CustomerResponse> search(@RequestParam String q,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return customerService.search(q, PageRequest.of(page, size));
    }

    @Operation(summary = "Update customer")
    @PutMapping("/{id}")
    public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateRequest req) {
        return customerService.update(id, req);
    }

    @Operation(summary = "Soft-delete customer")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
