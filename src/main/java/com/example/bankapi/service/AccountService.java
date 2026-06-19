package com.example.bankapi.service;

import com.example.bankapi.dto.account.AccountResponse;
import com.example.bankapi.dto.account.DepositRequest;
import com.example.bankapi.dto.account.TransferRequest;
import com.example.bankapi.dto.account.WithdrawRequest;
import com.example.bankapi.entity.AccountStatus;
import com.example.bankapi.exception.InsufficientFundsException;
import com.example.bankapi.exception.InvalidTransferException;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.entity.Account;
import com.example.bankapi.entity.BankTransaction;
import com.example.bankapi.entity.TransactionType;
import com.example.bankapi.repository.TransactionRepository;
import com.example.bankapi.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountResponse openAccount(Long ownerId) {
        String accountNumber;
        do {
            accountNumber = String.format("%012d", (long)(Math.random() * 1_000_000_000_000L));
        }
        while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .ownerId(ownerId)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(account);

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();

    }

    public List<AccountResponse> findMyAccounts(Long ownerId) {
        return accountRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(account -> AccountResponse.builder()
                        .id(account.getId())
                        .accountNumber(account.getAccountNumber())
                        .balance(account.getBalance())
                        .status(account.getStatus().name())
                        .createdAt(account.getCreatedAt())
                        .build())
                .toList();
    }

    public AccountResponse findMyAccount(Long id, Long ownerId) {
        Account account = accountRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public AccountResponse deposit(Long accountId, DepositRequest request, Long ownerId) {
        Account account = accountRepository.findByIdAndOwnerId(accountId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        BankTransaction transaction = BankTransaction.builder()
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .fromAccount(account)
                .description("Deposit")
                .build();

        transactionRepository.save(transaction);

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();
    }

    public AccountResponse withdraw(Long accountId, WithdrawRequest request, Long ownerId) {
        Account account = accountRepository.findByIdAndOwnerId(accountId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalStateException("Account is frozen");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is closed");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        BankTransaction transaction = BankTransaction.builder()
                .type(TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .fromAccount(account)
                .description("Withdrawal")
                .build();

        transactionRepository.save(transaction);

        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();
    }

    @Transactional
    public AccountResponse transfer(Long fromAccountId, TransferRequest request, Long ownerId) {
        Account source = accountRepository.findByIdAndOwnerId(fromAccountId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found"));

        Account destination = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found"));

        if(source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }

        if (source.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalStateException("Source account is frozen");
        }
        if (source.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Source account is closed");
        }

        if (destination.getStatus() == AccountStatus.FROZEN) {
            throw new IllegalStateException("Destination account is frozen");
        }

        if (destination.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Destination account is closed");
        }

        if (source.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        source.setBalance(source.getBalance().subtract(request.getAmount()));
        accountRepository.save(source);

        destination.setBalance(destination.getBalance().add(request.getAmount()));
        accountRepository.save(destination);

        BankTransaction transaction = BankTransaction.builder()
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .fromAccount(source)
                .toAccount(destination)
                .description(request.getDescription())
                .build();

        transactionRepository.save(transaction);

        return AccountResponse.builder()
                .id(source.getId())
                .accountNumber(source.getAccountNumber())
                .balance(source.getBalance())
                .status(source.getStatus().name())
                .createdAt(source.getCreatedAt())
                .build();
    }


}
