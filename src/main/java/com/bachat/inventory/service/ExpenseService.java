package com.bachat.inventory.service;

import com.bachat.inventory.domain.Expense;
import com.bachat.inventory.dto.ExpenseCreateRequest;
import com.bachat.inventory.dto.ExpenseResponse;
import com.bachat.inventory.dto.ExpenseUpdateRequest;
import com.bachat.inventory.exception.ResourceNotFoundException;
import com.bachat.inventory.repository.ExpenseRepository;
import com.bachat.inventory.util.MoneyUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse create(ExpenseCreateRequest req) {
        Expense e = new Expense();
        e.setTitle(req.getTitle().trim());
        e.setAmount(MoneyUtil.scale2(req.getAmount()));
        e.setExpenseDate(req.getExpenseDate());
        e.setDescription(req.getDescription());
        return toResponse(expenseRepository.save(e));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: id=" + id));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(LocalDate start, LocalDate end) {
        List<Expense> list;
        if (start != null && end != null) {
            list = expenseRepository.findAllByExpenseDateBetween(start, end);
        } else {
            list = expenseRepository.findAll();
        }
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseUpdateRequest req) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: id=" + id));

        e.setTitle(req.getTitle().trim());
        e.setAmount(MoneyUtil.scale2(req.getAmount()));
        e.setExpenseDate(req.getExpenseDate());
        e.setDescription(req.getDescription());

        return toResponse(expenseRepository.save(e));
    }

    @Transactional
    public void delete(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: id=" + id));
        expenseRepository.delete(e);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getAmount(), e.getExpenseDate(), e.getDescription(), e.getCreatedAt());
    }
}
