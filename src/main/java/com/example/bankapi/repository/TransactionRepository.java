package com.example.bankapi.repository;

import com.example.bankapi.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {

    // History for one account
    // regardless of whether it was the source or destination

    @Query("""
        SELECT t FROM BankTransaction t
        WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId
        ORDER BY t.occurredAt DESC
""")
    List<BankTransaction> findHistoryForAccount(@Param("accountId") Long accountId);
}
