package com.bachat.inventory.service;

import com.bachat.inventory.domain.Supplier;
import com.bachat.inventory.dto.SupplierRequest;
import com.bachat.inventory.dto.SupplierResponse;
import com.bachat.inventory.exception.ConflictException;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.PurchaseOrderRepository;
import com.bachat.inventory.repository.SupplierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepo;
    private final PurchaseOrderRepository poRepo;
    private final AuditService auditService;

    public SupplierService(SupplierRepository supplierRepo,
                           PurchaseOrderRepository poRepo,
                           AuditService auditService) {
        this.supplierRepo = supplierRepo;
        this.poRepo = poRepo;
        this.auditService = auditService;
    }

    @Transactional
    public SupplierResponse create(SupplierRequest req) {
        Supplier s = new Supplier();
        s.setName(req.getName().trim());
        s.setPhone(req.getPhone());
        s.setAddress(req.getAddress());
        s.setContactPerson(req.getContactPerson());
        s.setNotes(req.getNotes());
        Supplier saved = supplierRepo.save(s);

        auditService.log("SUPPLIER", saved.getId(), "CREATE", "Supplier created: " + saved.getName());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SupplierResponse get(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> list(Pageable pageable) {
        return supplierRepo.findByDeletedFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SupplierResponse> search(String q, Pageable pageable) {
        return supplierRepo.searchByNameOrPhone(q, pageable).map(this::toResponse);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest req) {
        Supplier s = findActive(id);
        String old = s.getName();
        s.setName(req.getName().trim());
        s.setPhone(req.getPhone());
        s.setAddress(req.getAddress());
        s.setContactPerson(req.getContactPerson());
        s.setNotes(req.getNotes());
        Supplier saved = supplierRepo.save(s);

        auditService.log("SUPPLIER", id, "UPDATE", "Supplier updated", old, saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Supplier s = findActive(id);
        if (poRepo.existsBySupplier_Id(id)) {
            // Soft delete if purchase history exists
            s.setDeleted(true);
            s.setDeletedAt(LocalDateTime.now());
            s.setDeletedBy(auditService.getCurrentUsername());
            supplierRepo.save(s);
            auditService.log("SUPPLIER", id, "SOFT_DELETE", "Supplier soft-deleted: " + s.getName());
        } else {
            supplierRepo.delete(s);
            auditService.log("SUPPLIER", id, "DELETE", "Supplier permanently deleted: " + s.getName());
        }
    }

    private Supplier findActive(Long id) {
        Supplier s = supplierRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: id=" + id));
        if (s.isDeleted()) throw new ResourceNotFoundException("Supplier has been deleted: id=" + id);
        return s;
    }

    private SupplierResponse toResponse(Supplier s) {
        SupplierResponse r = new SupplierResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setPhone(s.getPhone());
        r.setAddress(s.getAddress());
        r.setContactPerson(s.getContactPerson());
        r.setNotes(s.getNotes());
        r.setCreatedAt(s.getCreatedAt());
        return r;
    }
}
