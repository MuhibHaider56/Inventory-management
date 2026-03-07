package com.bachat.inventory.service;

import com.bachat.inventory.domain.Expense;
import com.bachat.inventory.dto.ExpenseCreateRequest;
import com.bachat.inventory.dto.ExpenseResponse;
import com.bachat.inventory.dto.ExpenseUpdateRequest;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.ExpenseRepository;
import com.bachat.inventory.util.MoneyUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final AuditService auditService;

    public ExpenseService(ExpenseRepository expenseRepository, AuditService auditService) {
        this.expenseRepository = expenseRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense e = new Expense();
        e.setTitle(req.getTitle().trim());
        e.setAmount(MoneyUtil.scale2(req.getAmount()));
        e.setExpenseDate(req.getExpenseDate() != null ? req.getExpenseDate() : LocalDate.now());
        e.setDescription(req.getDescription());
        ExpenseResponse resp = toResponse(expenseRepository.save(e));

        auditService.log("EXPENSE", resp.getId(), "CREATE",
                "Expense created: " + req.getTitle() + ", amount=" + req.getAmount());

        return resp;
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: id=" + id));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> list(LocalDate start, LocalDate end, Pageable pageable) {
        if (start != null && end != null) {
            return expenseRepository.findByExpenseDateBetween(start, end, pageable).map(this::toResponse);
        }
        return expenseRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: id=" + id));

        String old = e.getTitle() + " | " + e.getAmount();
        e.setTitle(req.getTitle().trim());
        e.setAmount(MoneyUtil.scale2(req.getAmount()));
        e.setExpenseDate(req.getExpenseDate());
        e.setDescription(req.getDescription());
        ExpenseResponse resp = toResponse(expenseRepository.save(e));

        auditService.log("EXPENSE", id, "UPDATE",
                "Expense updated", old, req.getTitle() + " | " + req.getAmount());

        return resp;
    }

    @Transactional
    public void delete(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: id=" + id));

        auditService.log("EXPENSE", id, "DELETE", "Expense deleted: " + e.getTitle());
        expenseRepository.delete(e);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(),
                e.getExpenseDate(), e.getDescription(), e.getCreatedAt());
    }
}
