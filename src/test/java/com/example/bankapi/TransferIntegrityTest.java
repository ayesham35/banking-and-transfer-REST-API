package com.example.bankapi;

import com.example.bankapi.dto.account.TransferRequest;
import com.example.bankapi.entity.AccountStatus;
import com.example.bankapi.repository.AccountRepository;
import com.example.bankapi.repository.TransactionRepository;
import com.example.bankapi.repository.UserRepository;
import com.example.bankapi.service.AccountService;
import com.example.bankapi.entity.User;
import com.example.bankapi.entity.Role;
import com.example.bankapi.entity.Account;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
public class TransferIntegrityTest {

    @Autowired private AccountService accountService;
    @Autowired private AccountRepository accountRepository;
    @MockitoBean private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    @Test
    void transferRollsBackOnFailure() {
        // Create a test user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setEmail("test@example.com");

        userRepository.save(user);

        // Create two accounts
        Account source = Account.builder()
                .accountNumber("111111111111")
                .owner(user)
                .balance(new BigDecimal("500.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        Account destination = Account.builder()
                .accountNumber("222222222222")
                .owner(user)
                .balance(new BigDecimal("100.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        accountRepository.save(source);
        accountRepository.save(destination);

        // Force transaction save to fail
        Mockito.doThrow(new RuntimeException("Forced failure"))
                .when(transactionRepository).save(Mockito.any());

        // Attempt the transfer - it should fail
        TransferRequest request = new TransferRequest();
        request.setToAccountNumber("222222222222");
        request.setAmount(new BigDecimal("200.00"));

        assertThrows(RuntimeException.class, () ->
                accountService.transfer(source.getId(), request, "testuser"));

        // Assert balances are unchanged
        Account updatedSource = accountRepository.findById(source.getId()).orElseThrow();
        Account updatedDestination = accountRepository.findById(destination.getId()).orElseThrow();

        assertEquals(0, updatedSource.getBalance().compareTo(new BigDecimal("500.00")));
        assertEquals(0, updatedDestination.getBalance().compareTo(new BigDecimal("100.00")));
    }

}
