package com.bachat.inventory.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceExpense {
    private String title;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
}