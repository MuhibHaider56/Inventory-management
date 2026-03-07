package com.bachat.inventory.controller;

import com.bachat.inventory.domain.OrderPayment;
import com.bachat.inventory.domain.OrderStatus;
import com.bachat.inventory.domain.PaymentStatus;
import com.bachat.inventory.dto.*;
import com.bachat.inventory.repository.OrderPaymentRepository;
import com.bachat.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Orders", description = "Place orders, manage order lifecycle, edit orders, and generate invoice data.")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentRepository orderPaymentRepository;

    public OrderController(OrderService orderService, OrderPaymentRepository orderPaymentRepository) {
        this.orderService = orderService;
        this.orderPaymentRepository = orderPaymentRepository;
    }

    @Operation(summary = "Place an order")
    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody OrderCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(req));
    }

    @Operation(summary = "Edit an existing order (replace items, recalculate totals)")
    @PutMapping("/{id}")
    public OrderResponse edit(@PathVariable Long id, @Valid @RequestBody OrderEditRequest req) {
        return orderService.editOrder(id, req);
    }

    @Operation(summary = "Get order by id")
    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @Operation(summary = "List orders (paginated, filterable, sortable, excludes soft-deleted)")
    @GetMapping
    public Page<OrderResponse> list(
            @Parameter(description = "Filter by customer ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Filter by status: PENDING, COMPLETED, CANCELLED") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Filter by payment status: UNPAID, PARTIALLY_PAID, PAID") @RequestParam(required = false) PaymentStatus paymentStatus,
            @Parameter(description = "Start date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End date filter") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @Parameter(description = "Sort field (e.g. orderDate, totalAmount)") @RequestParam(defaultValue = "orderDate") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return orderService.list(customerId, status, paymentStatus, start, end, PageRequest.of(page, size, sort));
    }

    @Operation(summary = "Cancel an order (restocks inventory)")
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @Operation(summary = "Soft-delete an order")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        orderService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get invoice data for an order")
    @GetMapping("/{orderId}/invoice")
    public InvoiceResponse invoice(@PathVariable Long orderId) {
        return orderService.getInvoice(orderId);
    }

    @Operation(summary = "Add payment to an order")
    @PostMapping("/{orderId}/payments")
    public ResponseEntity<OrderResponse> addPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentCreateRequest request) {
        return ResponseEntity.ok(orderService.addPayment(orderId, request));
    }

    @Operation(summary = "Get payments for an order")
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<List<OrderPaymentResponse>> getOrderPayments(@PathVariable Long orderId) {
        List<OrderPayment> payments = orderPaymentRepository.findByOrderIdOrderByPaymentDateAsc(orderId);
        List<OrderPaymentResponse> response = payments.stream().map(this::toPaymentResponse).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add expense to order")
    @PostMapping("/{orderId}/expenses")
    public ResponseEntity<OrderResponse> addExpense(
            @PathVariable Long orderId,
            @Valid @RequestBody ExpenseCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.addExpenseToOrder(orderId, req));
    }

    private OrderPaymentResponse toPaymentResponse(OrderPayment p) {
        OrderPaymentResponse r = new OrderPaymentResponse();
        r.setId(p.getId());
        r.setAmount(p.getAmount());
        r.setPaymentDate(p.getPaymentDate());
        r.setMethod(p.getMethod());
        r.setReference(p.getReference());
        r.setNote(p.getNote());
        return r;
    }
}
