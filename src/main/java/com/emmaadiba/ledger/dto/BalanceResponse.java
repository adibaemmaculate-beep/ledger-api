package com.emmaadiba.ledger.dto;

import java.math.BigDecimal;

public record BalanceResponse(String accountId, BigDecimal balance) {}
