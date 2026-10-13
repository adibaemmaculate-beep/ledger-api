package com.emmaadiba.ledger.controller;

import com.emmaadiba.ledger.dto.BalanceResponse;
import com.emmaadiba.ledger.dto.TransactionRequest;
import com.emmaadiba.ledger.dto.TransactionResponse;
import com.emmaadiba.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** HTTP layer only: parse the request, call the service, shape the response. */
@RestController
@RequestMapping("/api")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        return ledgerService.record(request);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> list(@RequestParam String accountId) {
        return ledgerService.history(accountId);
    }

    @GetMapping("/accounts/{accountId}/balance")
    public BalanceResponse balance(@PathVariable String accountId) {
        return new BalanceResponse(accountId, ledgerService.balance(accountId));
    }
}
