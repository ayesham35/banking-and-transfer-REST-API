package com.example.bankapi.controller.v2;

import com.example.bankapi.dto.account.AccountResponse;
import com.example.bankapi.dto.account.DepositRequest;
import com.example.bankapi.dto.account.WithdrawRequest;
import com.example.bankapi.dto.account.TransferRequest;
import com.example.bankapi.dto.account.OpenAccountRequest;
import com.example.bankapi.entity.User;
import com.example.bankapi.idempotency.IdempotencyRecord;
import com.example.bankapi.idempotency.IdempotencyService;
import com.example.bankapi.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal User user) {
        return accountService.openAccount(user.getUsername());
    }

    @GetMapping
    public List<AccountResponse> listAccounts(
            @AuthenticationPrincipal User user) {
        return accountService.findMyAccounts(user.getUsername());
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return accountService.findMyAccount(id, user.getUsername());
    }


    @PostMapping("/{id}/deposits")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody DepositRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpServletRequest) throws Exception {

                // Check for existing record
        Optional<IdempotencyRecord> existing = idempotencyService.findExisting(
                idempotencyKey, user, httpServletRequest.getRequestURI(), request);

        // If found, replay cached response
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            AccountResponse cached = objectMapper.readValue(
                    record.getResponseBody(), AccountResponse.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        // Perform real operation
        AccountResponse response = accountService.deposit(id, request, user.getUsername());

        // Record the result
        idempotencyService.record(
                idempotencyKey, user, httpServletRequest.getRequestURI(),
                request, 200, response);

        return ResponseEntity.ok(response);

    }

    @PostMapping("/{id}/withdrawals")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpServletRequest) throws Exception {

        // Check for existing record
        Optional<IdempotencyRecord> existing = idempotencyService.findExisting(
                idempotencyKey, user, httpServletRequest.getRequestURI(), request);

        // If found, replay cached response
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            AccountResponse cached = objectMapper.readValue(
                    record.getResponseBody(), AccountResponse.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        // Perform real operation
        AccountResponse response = accountService.withdraw(id, request, user.getUsername());

        // Record the result
        idempotencyService.record(
                idempotencyKey, user, httpServletRequest.getRequestURI(),
                request, 200, response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transfers")
    public ResponseEntity<AccountResponse> transfer(
            @PathVariable Long id,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody TransferRequest request,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpServletRequest) throws Exception {

        // Check for existing record
        Optional<IdempotencyRecord> existing = idempotencyService.findExisting(
                idempotencyKey, user, httpServletRequest.getRequestURI(), request);

        // If found, replay cached response
        if (existing.isPresent()) {
            IdempotencyRecord record = existing.get();
            AccountResponse cached = objectMapper.readValue(record.getResponseBody(), AccountResponse.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        // Perform real operation
        AccountResponse response = accountService.transfer(id, request, user.getUsername());

        // Record the result
        idempotencyService.record(idempotencyKey, user, httpServletRequest.getRequestURI(), request, 200, response);

        return ResponseEntity.ok(response);

    }


}
