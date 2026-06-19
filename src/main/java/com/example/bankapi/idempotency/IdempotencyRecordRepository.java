package com.example.bankapi.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKeyAndOwnerIdAndRequestPath(
            String idempotencyKey,
            Long ownerId,
            String requestPath
    );
}
