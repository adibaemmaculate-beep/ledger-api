package com.emmaadiba.ledger.service;

import com.emmaadiba.ledger.dto.TransactionRequest;
import com.emmaadiba.ledger.dto.TransactionResponse;
import com.emmaadiba.ledger.model.Transaction;
import com.emmaadiba.ledger.model.TransactionType;
import com.emmaadiba.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Business rules live here, not in the controller.
 * The repository is injected through the constructor (dependency injection),
 * which is what makes this class easy to unit test with a mock.
 */
@Service
public class LedgerService {

    private final TransactionRepository repository;

    public LedgerService(TransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Records a transaction. A debit that would overdraw the account is rejected.
     * Runs in one database transaction, so the balance check and the insert
     * succeed or fail together (atomicity).
     */
    @Transactional
    public TransactionResponse record(TransactionRequest request) {
        BigDecimal balance = repository.computeBalance(request.accountId());

        if (request.type() == TransactionType.DEBIT && balance.compareTo(request.amount()) < 0) {
            throw new InsufficientFundsException(request.accountId(), balance, request.amount());
        }

        Transaction saved = repository.save(new Transaction(
                request.accountId(), request.amount(), request.type(), request.description()));

        return TransactionResponse.from(saved, balance.add(saved.signedAmount()));
    }

    /** All transactions for an account, oldest first, each with the balance after it. O(n). */
    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String accountId) {
        List<TransactionResponse> result = new ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        for (Transaction t : repository.findByAccountIdOrderByCreatedAtAscIdAsc(accountId)) {
            running = running.add(t.signedAmount());
            result.add(TransactionResponse.from(t, running));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public BigDecimal balance(String accountId) {
        return repository.computeBalance(accountId);
    }
}
