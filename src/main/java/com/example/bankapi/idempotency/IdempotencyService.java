package com.example.bankapi.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRecordRepository repository;
    private final ObjectMapper objectMapper;

    public Optional<IdempotencyRecord> findExisting(
            String key, Long ownerId, String path, Object requestBody) {

        Optional<IdempotencyRecord> existing = repository.findByIdempotencyKeyAndOwnerIdAndRequestPath(key, ownerId, path);

        if (existing.isPresent()) {
            String incomingHash = hashBody(requestBody);
            if (!incomingHash.equals(existing.get().getRequestHash())) {
                throw new IdempotencyConflictException(
                        "Idempotency-Key '" + key + "' was previously used with a different request body.");
            }
        }

        return existing;
    }

    public void record(
            String key, Long ownerId, String path, Object requestBody,
            int responseStatus, Object responseBody) {

        IdempotencyRecord rec = IdempotencyRecord.builder()
                .idempotencyKey(key)
                .ownerId(ownerId)
                .requestPath(path)
                .requestHash(hashBody(requestBody))
                .responseStatus(responseStatus)
                .responseBody(serialize(responseBody))
                .build();

        repository.save(rec);
    }

    private String hashBody(Object body) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(body);
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(json);
            return Base64.getEncoder().encodeToString(hash);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to hash request body", e);
        }
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to serialize response body", e);
        }
    }
}
