package io.hydra.monolith.application.service;

import io.hydra.monolith.application.port.AccountRepository;
import io.hydra.monolith.application.port.LedgerRepository;
import io.hydra.monolith.application.port.TransactionRepository;
import io.hydra.monolith.application.usecase.ProcessPaymentCommand;
import io.hydra.monolith.application.usecase.ProcessPaymentResult;
import io.hydra.monolith.application.usecase.ProcessPaymentUseCase;
import io.hydra.monolith.domain.Account;
import io.hydra.monolith.domain.Currency;
import io.hydra.monolith.domain.EntryType;
import io.hydra.monolith.domain.IdempotencyKey;
import io.hydra.monolith.domain.LedgerEntry;
import io.hydra.monolith.domain.Money;
import io.hydra.monolith.domain.Transaction;
import io.hydra.monolith.domain.TransactionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ProcessPaymentService implements ProcessPaymentUseCase {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    public ProcessPaymentService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
    }

    @Override
    public ProcessPaymentResult handle(ProcessPaymentCommand command) {
        validateCommand(command);

        Account debitAccount = accountRepository.findById(command.debitAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.debitAccountId().value()));
        Account creditAccount = accountRepository.findById(command.creditAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.creditAccountId().value()));

        Money amount = command.amount().normalized();
        Account updatedDebit = debitAccount.debit(amount);
        Account updatedCredit = creditAccount.credit(amount);

        UUID transactionId = UUID.randomUUID();
        Instant now = Instant.now();
        Transaction transaction = new Transaction(
                transactionId,
                command.debitAccountId(),
                command.creditAccountId(),
                amount,
                TransactionStatus.COMPLETED,
                command.idempotencyKey(),
                now
        );

        LedgerEntry debitEntry = new LedgerEntry(
                UUID.randomUUID(),
                transactionId,
                command.debitAccountId(),
                EntryType.DEBIT,
                amount,
                now
        );
        LedgerEntry creditEntry = new LedgerEntry(
                UUID.randomUUID(),
                transactionId,
                command.creditAccountId(),
                EntryType.CREDIT,
                amount,
                now
        );

        transactionRepository.save(transaction);
        ledgerRepository.saveAll(List.of(debitEntry, creditEntry));
        accountRepository.saveAll(List.of(updatedDebit, updatedCredit));

        return new ProcessPaymentResult(
                transactionId,
                transaction.status(),
                command.debitAccountId().value(),
                command.creditAccountId().value(),
                amount.amount(),
                amount.currency()
        );
    }

    private void validateCommand(ProcessPaymentCommand command) {
        if (command.debitAccountId().equals(command.creditAccountId())) {
            throw new InvalidTransferException("Debit and credit accounts must differ");
        }
        if (command.amount().amount().signum() <= 0) {
            throw new InvalidTransferException("Amount must be positive");
        }
        if (command.currency() != Currency.BRL) {
            throw new InvalidCurrencyException(command.currency().name());
        }
        IdempotencyKey key = command.idempotencyKey();
        if (key.value().isBlank()) {
            throw new InvalidTransferException("Idempotency key is required");
        }
    }
}
