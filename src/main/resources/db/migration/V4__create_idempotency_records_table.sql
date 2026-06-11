CREATE TABLE idempotency_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    idempotency_key VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    request_hash VARCHAR(100) NOT NULL,
    response_status INT NOT NULL,
    response_body TEXT NOT NULL,
    request_path VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_idempotency_user (idempotency_key, user_id)
) ENGINE = InnoDB DEFAULT charset=utf8mb4;