package com.bachat.inventory.repository;

import com.bachat.inventory.domain.InvoiceSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from InvoiceSequence s where s.yearMonth = :ym")
    Optional<InvoiceSequence> findByYearMonthForUpdate(@Param("ym") String yearMonth);
}
