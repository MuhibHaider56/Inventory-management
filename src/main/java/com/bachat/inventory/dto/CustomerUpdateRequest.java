package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotBlank;

public class CustomerUpdateRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String phone;
    private String address;

    private java.math.BigDecimal creditLimit;

    public java.math.BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(java.math.BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
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
}
