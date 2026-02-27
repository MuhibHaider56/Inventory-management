package com.bachat.inventory.controller;

import com.bachat.inventory.domain.OrderPayment;
import com.bachat.inventory.dto.*;
import com.bachat.inventory.repository.OrderPaymentRepository;
import com.bachat.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Orders", description = "Place orders, manage order lifecycle, and generate invoice data.")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderPaymentRepository orderPaymentRepository;

    public OrderController(OrderService orderService, OrderPaymentRepository orderPaymentRepository) {
        this.orderService = orderService;
        this.orderPaymentRepository = orderPaymentRepository;
    }

    @Operation(
            summary = "Place an order",
            description = "Creates an order with items, checks available stock, deducts inventory, and calculates profit. Runs in a transaction."
    )
    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody OrderCreateRequest req) {
        OrderResponse created = orderService.placeOrder(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Get order by id", description = "Returns order details, including order items.")
    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orderService.get(id);
    }

    @Operation(summary = "List orders (paginated)")
    @GetMapping
    public Page<OrderResponse> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.list(pageable);
    }

    @Operation(
            summary = "Cancel an order",
            description = "Cancels an order and restores inventory quantities for its items (restock)."
    )
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @GetMapping("/{orderId}/invoice")
    public InvoiceResponse invoice(@PathVariable Long orderId) {
        return orderService.getInvoice(orderId);
    }

    @PostMapping("/{orderId}/payments")
    public ResponseEntity<OrderResponse> addPayment(
            @PathVariable Long orderId,
            @Valid @RequestBody PaymentCreateRequest request) {

        OrderResponse response = orderService.addPayment(orderId, request);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<List<OrderPaymentResponse>> getOrderPayments(
            @PathVariable Long orderId) {

        List<OrderPayment> payments =
                orderPaymentRepository.findByOrderIdOrderByPaymentDateAsc(orderId);

        List<OrderPaymentResponse> response = payments.stream()
                .map(this::toPaymentResponse)
                .toList();

        return ResponseEntity.ok(response);
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
