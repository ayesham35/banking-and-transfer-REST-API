package com.example.bankapi.controller.v2;

import com.example.bankapi.dto.transaction.TransactionResponse;
import com.example.bankapi.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v2/accounts")
@RequiredArgsConstructor
public class TransactionControllerV2 {

    private final TransactionService transactionService;

    @GetMapping("/{id}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long ownerId = Long.parseLong(jwt.getSubject());
        return transactionService.findHistoryForAccount(id, ownerId);
    }
}

