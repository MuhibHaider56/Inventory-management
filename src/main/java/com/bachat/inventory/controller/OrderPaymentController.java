package com.bachat.inventory.controller;


import com.bachat.inventory.dto.OrderPaymentResponse;
import com.bachat.inventory.domain.OrderPayment;
import com.bachat.inventory.repository.OrderPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class OrderPaymentController {

    private final OrderPaymentRepository orderPaymentRepository;

    // 1️⃣ Get payments of specific order
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderPaymentResponse>> getByOrder(
            @PathVariable Long orderId) {

        List<OrderPayment> payments =
                orderPaymentRepository.findByOrderIdOrderByPaymentDateAsc(orderId);

        return ResponseEntity.ok(
                payments.stream().map(this::map).toList()
        );
    }

    // 2️⃣ Get all payments
    @GetMapping
    public ResponseEntity<List<OrderPaymentResponse>> getAll() {

        List<OrderPayment> payments =
                orderPaymentRepository.findAll(Sort.by("paymentDate").descending());

        return ResponseEntity.ok(
                payments.stream().map(this::map).toList()
        );
    }

    // 3️⃣ Get payments between dates
    @GetMapping("/range")
    public ResponseEntity<List<OrderPaymentResponse>> getByRange(
            @RequestParam Instant start,
            @RequestParam Instant end) {

        List<OrderPayment> payments =
                orderPaymentRepository.findByPaymentDateBetween(start, end);

        return ResponseEntity.ok(
                payments.stream().map(this::map).toList()
        );
    }

    private OrderPaymentResponse map(OrderPayment p) {
        OrderPaymentResponse r = new OrderPaymentResponse();
        r.setId(p.getId());
        r.setOrderId(p.getOrder().getId());
        r.setAmount(p.getAmount());
        r.setPaymentDate(p.getPaymentDate());
        r.setMethod(p.getMethod());
        r.setReference(p.getReference());
        r.setNote(p.getNote());
        return r;
    }
}
