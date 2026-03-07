package com.bachat.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LedgerEntryResponse {

    private LocalDateTime date;
    private String type;  // ORDER, PAYMENT, RETURN, EXPENSE
    private String reference;  // invoice number, payment method, etc.
    private String description;
    private BigDecimal debit;   // amount owed (orders)
    private BigDecimal credit;  // amount paid (payments, refunds)
    private BigDecimal balance; // running balance

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getDebit() { return debit; }
    public void setDebit(BigDecimal debit) { this.debit = debit; }

    public BigDecimal getCredit() { return credit; }
    public void setCredit(BigDecimal credit) { this.credit = credit; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
