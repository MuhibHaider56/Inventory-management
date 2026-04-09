-- Add payment tracking fields to purchase_orders
ALTER TABLE purchase_orders
    ADD COLUMN payment_due_date DATE,
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID';

-- Backfill payment_status for existing records
UPDATE purchase_orders
SET payment_status = CASE
    WHEN amount_paid >= total_amount AND total_amount > 0 THEN 'PAID'
    WHEN amount_paid > 0 THEN 'PARTIALLY_PAID'
    ELSE 'UNPAID'
END;

-- Payment history table for purchase orders
CREATE TABLE purchase_order_payments (
    id                BIGSERIAL PRIMARY KEY,
    purchase_order_id BIGINT        NOT NULL REFERENCES purchase_orders(id),
    amount            NUMERIC(12,2) NOT NULL,
    payment_date      TIMESTAMP     NOT NULL DEFAULT NOW(),
    method            VARCHAR(50),
    reference         VARCHAR(255),
    note              TEXT,
    recorded_by       VARCHAR(50),
    CONSTRAINT chk_pop_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_pop_purchase_order ON purchase_order_payments(purchase_order_id);

-- Backfill payment history from existing paid/partial POs
INSERT INTO purchase_order_payments (purchase_order_id, amount, payment_date, method, reference, note, recorded_by)
SELECT id, amount_paid, COALESCE(payment_date::TIMESTAMP, created_at), payment_method, payment_reference,
       'Migrated from initial payment', created_by
FROM purchase_orders
WHERE amount_paid > 0;
