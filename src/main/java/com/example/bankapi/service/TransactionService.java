package com.example.bankapi.service;

import com.example.bankapi.dto.transaction.TransactionResponse;
import com.example.bankapi.entity.Account;
import com.example.bankapi.exception.ResourceNotFoundException;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public List<TransactionResponse> findHistoryForAccount(Long accountId, String username) {
        Account account = accountRepository.findByIdAndOwner_Username(accountId, username)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return transactionRepository.findHistoryForAccount(account.getId())
                .stream()
                .map(t -> TransactionResponse.builder()
                        .id(t.getId())
                        .type(t.getType().name())
                        .amount(t.getAmount())
                        .fromAccountNumber(t.getFromAccount().getAccountNumber())
                        .toAccountNumber(t.getToAccount() != null ? t.getToAccount().getAccountNumber() : null)
                        .description(t.getDescription())
                        .occurredAt(t.getOccurredAt())
                        .build())
                .toList();
    }
}
