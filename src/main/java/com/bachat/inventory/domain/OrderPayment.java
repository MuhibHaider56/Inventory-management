package com.bachat.inventory.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_payments")
public class OrderPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder order;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();
    private String method;

    private String reference;

    @Column(columnDefinition = "TEXT")
    private String note;

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public SalesOrder getOrder() {
        return order;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public String getMethod() {
        return method;
    }

    public String getReference() {
        return reference;
    }

    public String getNote() {
        return note;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrder(SalesOrder order) {
        this.order = order;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
