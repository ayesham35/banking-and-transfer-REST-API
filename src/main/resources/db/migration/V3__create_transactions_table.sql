CREATE TABLE bank_transaction
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(14, 2) NOT NULL,
    from_account_id BIGINT,
    to_account_id BIGINT,
    description VARCHAR(255),
    occurred_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_txn_from (from_account_id),
    INDEX idx_txn_to (to_account_id)
) ENGINE = InnoDB DEFAULT charset=utf8mb4;