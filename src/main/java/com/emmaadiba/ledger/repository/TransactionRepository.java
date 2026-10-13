package com.emmaadiba.ledger.repository;

import com.emmaadiba.ledger.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Spring Data generates the implementation at runtime from the method names.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByCreatedAtAscIdAsc(String accountId);

    // Let the database do the aggregation instead of loading every row.
    @Query("""
            SELECT COALESCE(SUM(CASE WHEN t.type = com.emmaadiba.ledger.model.TransactionType.CREDIT
                                     THEN t.amount ELSE -t.amount END), 0)
            FROM Transaction t
            WHERE t.accountId = :accountId
            """)
    BigDecimal computeBalance(@Param("accountId") String accountId);
}
