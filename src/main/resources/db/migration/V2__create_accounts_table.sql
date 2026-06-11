CREATE TABLE accounts
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL,
    owner_id BIGINT NOT NULL ,
    balance DECIMAL(14, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_accounts_number (account_number),
    INDEX idx_accounts_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;