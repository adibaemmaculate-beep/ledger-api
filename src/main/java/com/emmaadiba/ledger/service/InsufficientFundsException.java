package com.emmaadiba.ledger.service;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountId, BigDecimal balance, BigDecimal requested) {
        super("Account " + accountId + " has balance " + balance + " but debit of " + requested + " was requested");
    }
}
