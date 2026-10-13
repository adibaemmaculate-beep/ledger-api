package com.emmaadiba.ledger.dto;

import com.emmaadiba.ledger.model.Transaction;
import com.emmaadiba.ledger.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/** What the API returns. Keeps the database entity out of the public contract. */
public record TransactionResponse(
        Long id,
        String accountId,
        BigDecimal amount,
        TransactionType type,
        String description,
        Instant createdAt,
        BigDecimal runningBalance
) {
    public static TransactionResponse from(Transaction t, BigDecimal runningBalance) {
        return new TransactionResponse(t.getId(), t.getAccountId(), t.getAmount(), t.getType(),
                t.getDescription(), t.getCreatedAt(), runningBalance);
    }
}
