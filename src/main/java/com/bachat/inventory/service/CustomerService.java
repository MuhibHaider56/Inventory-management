package com.bachat.inventory.service;

import com.bachat.inventory.domain.Customer;
import com.bachat.inventory.dto.CustomerCreateRequest;
import com.bachat.inventory.dto.CustomerResponse;
import com.bachat.inventory.dto.CustomerUpdateRequest;
import com.bachat.inventory.exception.ConflictException;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.CustomerRepository;
import com.bachat.inventory.repository.SalesOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;

    public CustomerService(CustomerRepository customerRepository, SalesOrderRepository salesOrderRepository) {
        this.customerRepository = customerRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Transactional
    public CustomerResponse create(CustomerCreateRequest req) {
        Customer c = new Customer();
        c.setName(req.getName().trim());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        return toResponse(customerRepository.save(c));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Pageable pageable) {
        return customerRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerUpdateRequest req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));

        c.setName(req.getName().trim());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());

        return toResponse(customerRepository.save(c));
    }

    @Transactional
    public void delete(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));

        boolean hasOrders = salesOrderRepository.existsByCustomer_Id(id);
        if (hasOrders) {
            throw new ConflictException("Cannot delete customer because order history exists. Consider disabling the customer instead.");
        }

        customerRepository.delete(c);
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getPhone(), c.getAddress(), c.getCreatedAt());
    }
}
