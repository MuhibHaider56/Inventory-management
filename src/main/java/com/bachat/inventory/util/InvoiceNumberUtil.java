package com.bachat.inventory.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class InvoiceNumberUtil {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private InvoiceNumberUtil() {}

    /**
     * Simple deterministic invoice number:
     * INV-YYYYMMDD-000001 (uses orderId for last part)
     */
    public static String forOrder(Long orderId, LocalDateTime orderDate) {
        String date = orderDate.format(DATE);
        String seq = String.format("%06d", orderId == null ? 0 : orderId);
        return "INV-" + date + "-" + seq;
    }
}
