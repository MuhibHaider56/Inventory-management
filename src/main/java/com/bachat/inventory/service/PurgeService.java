package com.bachat.inventory.service;

import com.bachat.inventory.domain.Customer;
import com.bachat.inventory.domain.Product;
import com.bachat.inventory.domain.SalesOrder;
import com.bachat.inventory.repository.CustomerRepository;
import com.bachat.inventory.repository.ProductRepository;
import com.bachat.inventory.repository.SalesOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurgeService {

    private static final Logger log = LoggerFactory.getLogger(PurgeService.class);

    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final SalesOrderRepository orderRepo;
    private final AuditService auditService;

    @Value("${app.purge.retention-days:90}")
    private int retentionDays;

    public PurgeService(CustomerRepository customerRepo,
                        ProductRepository productRepo,
                        SalesOrderRepository orderRepo,
                        AuditService auditService) {
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
        this.auditService = auditService;
    }

    /**
     * Runs every night at 2:00 AM.
     * Permanently deletes soft-deleted records older than retention period.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldSoftDeletedRecords() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        log.info("Purge job started. Removing soft-deleted records older than {} days (before {})",
                retentionDays, cutoff);

        int totalPurged = 0;

        // Purge customers
        List<Customer> customers = customerRepo.findByDeletedTrueAndDeletedAtBefore(cutoff);
        if (!customers.isEmpty()) {
            customerRepo.deleteAll(customers);
            totalPurged += customers.size();
            log.info("Purged {} customers", customers.size());
        }

        // Purge products
        List<Product> products = productRepo.findByDeletedTrueAndDeletedAtBefore(cutoff);
        if (!products.isEmpty()) {
            productRepo.deleteAll(products);
            totalPurged += products.size();
            log.info("Purged {} products", products.size());
        }

        // Purge orders
        List<SalesOrder> orders = orderRepo.findByDeletedTrueAndDeletedAtBefore(cutoff);
        if (!orders.isEmpty()) {
            orderRepo.deleteAll(orders);
            totalPurged += orders.size();
            log.info("Purged {} orders", orders.size());
        }

        if (totalPurged > 0) {
            auditService.log("SYSTEM", null, "PURGE",
                    "Nightly purge completed: " + totalPurged + " records permanently deleted"
                            + " (customers=" + customers.size()
                            + ", products=" + products.size()
                            + ", orders=" + orders.size() + ")");
        }

        log.info("Purge job finished. Total records purged: {}", totalPurged);
    }
}
