package com.example.bankapi.controller.v1;

import com.example.bankapi.dto.account.*;
import com.example.bankapi.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
            @RequestBody OpenAccountRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return accountService.openAccount(user.getUsername());
    }

    @GetMapping
    public List<AccountResponse> listAccounts(
            @AuthenticationPrincipal UserDetails user) {
        return accountService.findMyAccounts(user.getUsername());
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return accountService.findMyAccount(id, user.getUsername());
    }

    @PostMapping("/{id}/deposits")
    public AccountResponse deposit(
            @PathVariable Long id,
            @RequestBody DepositRequest request,
            @AuthenticationPrincipal UserDetails user
            ) {
        return accountService.deposit(id, request, user.getUsername());
    }

    @PostMapping("/{id}/withdrawals")
    public AccountResponse withdraw(
            @PathVariable Long id,
            @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal UserDetails user
            ) {
        return accountService.withdraw(id, request, user.getUsername());
    }

    @PostMapping("/{id}/transfers")
    public AccountResponse transfer(
            @PathVariable Long id,
            @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails user
            ) {
        return accountService.transfer(id, request, user.getUsername());
    }

}
