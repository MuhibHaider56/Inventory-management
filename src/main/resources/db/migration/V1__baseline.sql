-- V1__baseline.sql
-- Baseline migration: captures the full schema as of Batch 1 completion.
-- Flyway will run this only on fresh databases (existing ones use baseline-on-migrate).

CREATE TABLE IF NOT EXISTS app_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL,
    last_login DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_app_users_username UNIQUE (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    credit_limit DECIMAL(12,2),
    deleted BIT NOT NULL DEFAULT 0,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(50),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_customer_name (name),
    INDEX idx_customer_phone (phone),
    INDEX idx_customer_deleted (deleted)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    cost_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    deleted BIT NOT NULL DEFAULT 0,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(50),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_product_name (name),
    INDEX idx_product_deleted (deleted)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity_available DECIMAL(10,2) NOT NULL,
    last_updated DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_number VARCHAR(20),
    customer_id BIGINT NOT NULL,
    order_date DATETIME(6) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    total_profit DECIMAL(12,2) NOT NULL,
    total_expenses DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_returns DECIMAL(12,2) NOT NULL DEFAULT 0,
    amount_paid DECIMAL(12,2) NOT NULL DEFAULT 0,
    payment_due_date DATE,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_by VARCHAR(50),
    deleted BIT NOT NULL DEFAULT 0,
    deleted_at DATETIME(6),
    deleted_by VARCHAR(50),
    PRIMARY KEY (id),
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT uk_order_invoice UNIQUE (invoice_number),
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_date (order_date),
    INDEX idx_order_status (status),
    INDEX idx_order_payment_status (payment_status),
    INDEX idx_order_deleted (deleted)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    profit DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS order_payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_date DATETIME(6) NOT NULL,
    method VARCHAR(255),
    reference VARCHAR(255),
    note TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    amount DECIMAL(12,2) NOT NULL,
    expense_date DATE NOT NULL,
    order_id BIGINT,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_expense_order FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_expense_date (expense_date),
    INDEX idx_expense_order (order_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS invoice_sequences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    `year_month` VARCHAR(7) NOT NULL,
    last_number INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_invoice_seq_ym UNIQUE (`year_month`)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(30) NOT NULL,
    username VARCHAR(50) NOT NULL,
    details TEXT,
    old_value TEXT,
    new_value TEXT,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_username (username),
    INDEX idx_audit_created (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales_returns (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    return_date DATETIME(6) NOT NULL,
    reason TEXT,
    refund_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    refund_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(50) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_return_order FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_return_order (order_id),
    INDEX idx_return_date (return_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales_return_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    return_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    restock BIT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    CONSTRAINT fk_return_item_return FOREIGN KEY (return_id) REFERENCES sales_returns(id),
    CONSTRAINT fk_return_item_order_item FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    CONSTRAINT fk_return_item_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;
