package com.bachat.inventory.service;

import com.bachat.inventory.domain.Customer;
import com.bachat.inventory.dto.CustomerCreateRequest;
import com.bachat.inventory.dto.CustomerResponse;
import com.bachat.inventory.dto.CustomerUpdateRequest;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    public CustomerService(CustomerRepository customerRepository, AuditService auditService) {
        this.customerRepository = customerRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest req) {
        Customer c = new Customer();
        c.setName(req.getName().trim());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setCreditLimit(req.getCreditLimit());
        Customer saved = customerRepository.save(c);

        auditService.log("CUSTOMER", saved.getId(), "CREATE",
                "Customer created: " + saved.getName());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        Customer c = findActiveById(id);
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepository.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(String query, Pageable pageable) {
        return customerRepository.searchByNameOrPhone(query, pageable).map(this::toResponse);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest req) {
        Customer c = findActiveById(id);

        String oldName = c.getName();
        c.setName(req.getName().trim());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setCreditLimit(req.getCreditLimit());

        Customer saved = customerRepository.save(c);

        auditService.log("CUSTOMER", saved.getId(), "UPDATE",
                "Customer updated", oldName, saved.getName());

        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Customer c = findActiveById(id);

        c.setDeleted(true);
        c.setDeletedAt(LocalDateTime.now());
        c.setDeletedBy(auditService.getCurrentUsername());
        customerRepository.save(c);

        auditService.log("CUSTOMER", id, "SOFT_DELETE",
                "Customer soft-deleted: " + c.getName());
    }

    private Customer findActiveById(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
        if (c.isDeleted()) {
            throw new ResourceNotFoundException("Customer has been deleted: id=" + id);
        }
        return c;
    }

    private CustomerResponse toResponse(Customer c) {
        CustomerResponse res = new CustomerResponse(
                c.getId(), c.getName(), c.getPhone(), c.getAddress(), c.getCreatedAt());
        res.setCreditLimit(c.getCreditLimit());
        return res;
    }
}
