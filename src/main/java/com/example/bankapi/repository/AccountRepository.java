package com.example.bankapi.repository;

import com.example.bankapi.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByOwner_UsernameOrderByCreatedAtDesc(String username);

    Optional<Account> findByIdAndOwner_Username(Long id, String username);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

}
