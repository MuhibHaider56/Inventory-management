package com.bachat.inventory.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_available", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantityAvailable;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    public Inventory() {}

    public Inventory(Product product, BigDecimal quantityAvailable) {
        this.product = product;
        this.quantityAvailable = quantityAvailable;
    }

    @PrePersist
    public void prePersist() {
        if (quantityAvailable == null) {
            quantityAvailable = BigDecimal.ZERO;
        }
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getQuantityAvailable() {
        return quantityAvailable;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setQuantityAvailable(BigDecimal quantityAvailable) {
        this.quantityAvailable = quantityAvailable;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
