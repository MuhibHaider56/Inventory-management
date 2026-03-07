package com.bachat.inventory.domain;

import com.bachat.inventory.util.MoneyUtil;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_date", columnList = "order_date"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_payment_status", columnList = "payment_status"),
    @Index(name = "idx_order_deleted", columnList = "deleted"),
    @Index(name = "idx_order_invoice", columnList = "invoice_number", unique = true)
})
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", unique = true, length = 20)
    private String invoiceNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "total_profit", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalProfit;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "total_expenses", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    @Column(name = "total_returns", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalReturns = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 50)
    private String deletedBy;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    public SalesOrder() {}

    @PrePersist
    public void prePersist() {
        if (orderDate == null) orderDate = LocalDateTime.now();
        if (status == null) status = OrderStatus.COMPLETED;
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (totalProfit == null) totalProfit = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        if (totalReturns == null) totalReturns = BigDecimal.ZERO;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }

    public void clearItems() {
        for (OrderItem item : items) {
            item.setOrder(null);
        }
        items.clear();
    }

    @Transient
    public BigDecimal getBalanceDue() {
        return MoneyUtil.subtract(this.totalAmount, this.amountPaid);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalProfit() { return totalProfit; }
    public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public LocalDate getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(LocalDate paymentDueDate) { this.paymentDueDate = paymentDueDate; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getTotalExpenses() { return totalExpenses; }
    public void setTotalExpenses(BigDecimal totalExpenses) { this.totalExpenses = totalExpenses; }

    public BigDecimal getTotalReturns() { return totalReturns; }
    public void setTotalReturns(BigDecimal totalReturns) { this.totalReturns = totalReturns; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
