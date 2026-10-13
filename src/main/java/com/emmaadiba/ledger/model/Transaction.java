package com.emmaadiba.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One immutable ledger entry. Balances are never stored; they are derived
 * from the sum of entries, which is how real ledgers stay auditable.
 */
@Entity
@Table(name = "transactions", indexes = @Index(columnList = "accountId"))
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountId;

    // BigDecimal, never double: 0.1 + 0.2 != 0.3 in floating point.
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String description;

    @Column(nullable = false)
    private Instant createdAt;

    protected Transaction() {
        // required by JPA
    }

    public Transaction(String accountId, BigDecimal amount, TransactionType type, String description) {
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.createdAt = Instant.now();
    }

    /** +amount for credits, -amount for debits. */
    public BigDecimal signedAmount() {
        return type == TransactionType.CREDIT ? amount : amount.negate();
    }

    public Long getId() { return id; }
    public String getAccountId() { return accountId; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
