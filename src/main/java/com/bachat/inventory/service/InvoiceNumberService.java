package com.bachat.inventory.service;

import com.bachat.inventory.domain.InvoiceSequence;
import com.bachat.inventory.repository.InvoiceSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceNumberService {

    private static final DateTimeFormatter YM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final InvoiceSequenceRepository seqRepo;

    public InvoiceNumberService(InvoiceSequenceRepository seqRepo) {
        this.seqRepo = seqRepo;
    }

    /**
     * Generates a sequential invoice number like INV-202603-0001.
     * Thread-safe via pessimistic locking on the sequence row.
     */
    @Transactional
    public String nextInvoiceNumber(LocalDateTime orderDate) {
        String ym = orderDate.format(YM_FORMAT);

        InvoiceSequence seq = seqRepo.findByYearMonthForUpdate(ym).orElse(null);

        if (seq == null) {
            seq = new InvoiceSequence(ym, 0);
        }

        seq.setLastNumber(seq.getLastNumber() + 1);
        seqRepo.save(seq);

        return String.format("INV-%s-%04d", ym.replace("-", ""), seq.getLastNumber());
    }
}
