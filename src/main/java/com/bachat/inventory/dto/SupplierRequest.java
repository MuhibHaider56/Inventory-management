package com.bachat.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class SupplierRequest {

    @NotBlank(message = "name is required")
    private String name;
    private String phone;
    private String address;
    private String contactPerson;
    private String notes;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
