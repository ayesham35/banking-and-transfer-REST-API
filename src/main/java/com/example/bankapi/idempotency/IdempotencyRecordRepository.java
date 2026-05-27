package com.example.bankapi.idempotency;

import com.example.bankapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKeyAndUserAndRequestPath(
            String idempotencyKey,
            User user,
            String requestPath
    );
}
