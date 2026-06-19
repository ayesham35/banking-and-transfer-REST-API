package com.example.bankapi.controller.v1;

import com.example.bankapi.dto.account.*;
import com.example.bankapi.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountControllerV1 {

    private final AccountService accountService;

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
    public AccountResponse deposit(
            @PathVariable Long id,
            @RequestBody DepositRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long ownerId = Long.parseLong(jwt.getSubject());
        return accountService.deposit(id, request, ownerId);
    }

    @PostMapping("/{id}/withdrawals")
    public AccountResponse withdraw(
            @PathVariable Long id,
            @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long ownerId = Long.parseLong(jwt.getSubject());
        return accountService.withdraw(id, request, ownerId);
    }

    @PostMapping("/{id}/transfers")
    public AccountResponse transfer(
            @PathVariable Long id,
            @RequestBody TransferRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        Long ownerId = Long.parseLong(jwt.getSubject());
        return accountService.transfer(id, request, ownerId);
    }

}
