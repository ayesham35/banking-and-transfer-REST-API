package com.example.bankapi.controller.v2;

import com.example.bankapi.dto.account.AccountResponse;
import com.example.bankapi.dto.account.DepositRequest;
import com.example.bankapi.dto.account.WithdrawRequest;
import com.example.bankapi.dto.account.TransferRequest;
import com.example.bankapi.idempotency.IdempotencyRecord;
import com.example.bankapi.idempotency.IdempotencyService;
import com.example.bankapi.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/v2/accounts")
@RequiredArgsConstructor
public class AccountControllerV2 {

    private final AccountService accountService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse openAccount(
            @AuthenticationPrincipal Jwt jwt) {
        Long ownerId = Long.parseLong(jwt.getSubject());
        return accountService.openAccount(ownerId);
    }

    @GetMapping
    public List<AccountResponse> listAccounts(
            @AuthenticationPrincipal Jwt jwt) {
        Long ownerId = Long.parseLong(jwt.getSubject());
        return accountService.findMyAccounts(ownerId);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long ownerId = Long.parseLong(jwt.getSubject());
        return accountService.findMyAccount(id, ownerId);
    }


    @PostMapping("/{id}/deposits")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody DepositRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest) throws Exception {

        Long ownerId = Long.parseLong(jwt.getSubject());

                // Check for existing record
        Optional<IdempotencyRecord> existing = idempotencyService.findExisting(
                idempotencyKey, ownerId, httpServletRequest.getRequestURI(), request);

        // If found, replay cached response
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            AccountResponse cached = objectMapper.readValue(
                    record.getResponseBody(), AccountResponse.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        // Perform real operation
        AccountResponse response = accountService.deposit(id, request, ownerId);

        // Record the result
        idempotencyService.record(
                idempotencyKey, ownerId, httpServletRequest.getRequestURI(),
                request, 200, response);

        return ResponseEntity.ok(response);

    }

    @PostMapping("/{id}/withdrawals")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest) throws Exception {

        Long ownerId = Long.parseLong(jwt.getSubject());

        // Check for existing record
        Optional<IdempotencyRecord> existing = idempotencyService.findExisting(
                idempotencyKey, ownerId, httpServletRequest.getRequestURI(), request);

        // If found, replay cached response
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            AccountResponse cached = objectMapper.readValue(
                    record.getResponseBody(), AccountResponse.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        // Perform real operation
        AccountResponse response = accountService.withdraw(id, request, ownerId);

        // Record the result
        idempotencyService.record(
                idempotencyKey, ownerId, httpServletRequest.getRequestURI(),
                request, 200, response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transfers")
    public ResponseEntity<AccountResponse> transfer(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpServletRequest) throws Exception {

        Long ownerId = Long.parseLong(jwt.getSubject());

        // Check for existing record
        Optional<IdempotencyRecord> existing = idempotencyService.findExisting(
                idempotencyKey, ownerId, httpServletRequest.getRequestURI(), request);

        // If found, replay cached response
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            AccountResponse cached = objectMapper.readValue(record.getResponseBody(), AccountResponse.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        // Perform real operation
        AccountResponse response = accountService.transfer(id, request, ownerId);

        // Record the result
        idempotencyService.record(idempotencyKey, ownerId, httpServletRequest.getRequestURI(), request, 200, response);

        return ResponseEntity.ok(response);

    }


}
