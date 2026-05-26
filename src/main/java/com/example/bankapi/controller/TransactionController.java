package com.example.bankapi.controller;

import com.example.bankapi.dto.transaction.TransactionResponse;
import com.example.bankapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user
            ) {
        return transactionService.findHistoryForAccount(id, user.getUsername());
    }
}
