package com.bachat.inventory.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "invoice_sequences")
public class InvoiceSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`year_month`", nullable = false, unique = true, length = 7)
    private String yearMonth; // "2026-03"

    @Column(name = "last_number", nullable = false)
    private int lastNumber;

    public InvoiceSequence() {}

    public InvoiceSequence(String yearMonth, int lastNumber) {
        this.yearMonth = yearMonth;
        this.lastNumber = lastNumber;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public int getLastNumber() { return lastNumber; }
    public void setLastNumber(int lastNumber) { this.lastNumber = lastNumber; }
}
