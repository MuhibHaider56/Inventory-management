package com.bachat.inventory.dto;

import java.time.LocalDateTime;

public class CustomerResponse {

    private Long id;
    private String name;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
    private java.math.BigDecimal creditLimit;

    public java.math.BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(java.math.BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public CustomerResponse() {}

    public CustomerResponse(Long id, String name, String phone, String address, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
