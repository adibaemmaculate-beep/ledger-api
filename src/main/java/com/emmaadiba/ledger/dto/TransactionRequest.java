package com.emmaadiba.ledger.dto;

import com.emmaadiba.ledger.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** What a client sends to POST /transactions. Validated before it reaches the service. */
public record TransactionRequest(
        @NotBlank String accountId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotNull TransactionType type,
        @Size(max = 255) String description
) {}
