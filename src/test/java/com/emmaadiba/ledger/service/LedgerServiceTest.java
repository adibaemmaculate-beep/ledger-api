package com.emmaadiba.ledger.service;

import com.emmaadiba.ledger.dto.TransactionRequest;
import com.emmaadiba.ledger.dto.TransactionResponse;
import com.emmaadiba.ledger.model.Transaction;
import com.emmaadiba.ledger.model.TransactionType;
import com.emmaadiba.ledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pure unit tests: the repository is mocked, so no Spring context or database is started. */
class LedgerServiceTest {

    private TransactionRepository repository;
    private LedgerService service;

    @BeforeEach
    void setUp() {
        repository = mock(TransactionRepository.class);
        service = new LedgerService(repository);
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void creditIncreasesBalance() {
        when(repository.computeBalance("ACC1")).thenReturn(new BigDecimal("100.00"));

        TransactionResponse r = service.record(
                new TransactionRequest("ACC1", new BigDecimal("50.00"), TransactionType.CREDIT, "deposit"));

        assertThat(r.runningBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void debitWithinBalanceSucceeds() {
        when(repository.computeBalance("ACC1")).thenReturn(new BigDecimal("100.00"));

        TransactionResponse r = service.record(
                new TransactionRequest("ACC1", new BigDecimal("100.00"), TransactionType.DEBIT, "rent"));

        assertThat(r.runningBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void overdraftIsRejectedAndNothingIsSaved() {
        when(repository.computeBalance("ACC1")).thenReturn(new BigDecimal("20.00"));

        assertThatThrownBy(() -> service.record(
                new TransactionRequest("ACC1", new BigDecimal("20.01"), TransactionType.DEBIT, "too much")))
                .isInstanceOf(InsufficientFundsException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void historyComputesRunningBalanceInOrder() {
        when(repository.findByAccountIdOrderByCreatedAtAscIdAsc("ACC1")).thenReturn(List.of(
                new Transaction("ACC1", new BigDecimal("100.00"), TransactionType.CREDIT, "a"),
                new Transaction("ACC1", new BigDecimal("30.00"), TransactionType.DEBIT, "b"),
                new Transaction("ACC1", new BigDecimal("5.50"), TransactionType.CREDIT, "c")));

        List<TransactionResponse> history = service.history("ACC1");

        assertThat(history).extracting(TransactionResponse::runningBalance)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("100.00"), new BigDecimal("70.00"), new BigDecimal("75.50"));
    }

    @Test
    void emptyAccountHasEmptyHistory() {
        when(repository.findByAccountIdOrderByCreatedAtAscIdAsc("NEW")).thenReturn(List.of());
        assertThat(service.history("NEW")).isEmpty();
    }
}
