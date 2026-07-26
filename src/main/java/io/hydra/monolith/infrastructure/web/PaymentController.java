package io.hydra.monolith.infrastructure.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hydra.monolith.application.service.AccountNotFoundException;
import io.hydra.monolith.application.service.InvalidCurrencyException;
import io.hydra.monolith.application.service.InvalidTransferException;
import io.hydra.monolith.application.usecase.ProcessPaymentCommand;
import io.hydra.monolith.application.usecase.ProcessPaymentResult;
import io.hydra.monolith.application.usecase.ProcessPaymentUseCase;
import io.hydra.monolith.domain.AccountId;
import io.hydra.monolith.domain.Currency;
import io.hydra.monolith.domain.IdempotencyKey;
import io.hydra.monolith.domain.InsufficientFundsException;
import io.hydra.monolith.domain.Money;
import io.hydra.monolith.infrastructure.idempotency.IdempotencyConflictException;
import io.hydra.monolith.infrastructure.idempotency.IdempotencyDecision;
import io.hydra.monolith.infrastructure.idempotency.IdempotencyManager;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class PaymentController {
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final IdempotencyManager idempotencyManager;
    private final RequestHasher requestHasher;
    private final ObjectMapper objectMapper;

    public PaymentController(
            ProcessPaymentUseCase processPaymentUseCase,
            IdempotencyManager idempotencyManager,
            RequestHasher requestHasher,
            ObjectMapper objectMapper
    ) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.idempotencyManager = idempotencyManager;
        this.requestHasher = requestHasher;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/payment")
    public ResponseEntity<?> processPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PaymentRequest request
    ) {
        IdempotencyKey key;
        try {
            key = new IdempotencyKey(idempotencyKey);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(error("INVALID_IDEMPOTENCY_KEY", ex.getMessage()));
        }
        String requestHash = requestHasher.hash(request);
        IdempotencyDecision decision;
        try {
            decision = idempotencyManager.evaluate(key, requestHash);
        } catch (IdempotencyConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(error("IDEMPOTENCY_CONFLICT", ex.getMessage()));
        }

        if (decision.state() == IdempotencyDecision.State.REPLAY) {
            return ResponseEntity.status(decision.responseStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(decision.responseBody());
        }
        if (decision.state() == IdempotencyDecision.State.IN_PROGRESS) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(error("IDEMPOTENCY_IN_PROGRESS", "Idempotency key is being processed"));
        }

        try {
            Currency currency = resolveCurrency(request.currency());
            ProcessPaymentCommand command = new ProcessPaymentCommand(
                    new AccountId(request.debitAccountId()),
                    new AccountId(request.creditAccountId()),
                    new Money(request.amount(), currency),
                    currency,
                    key
            );
            ProcessPaymentResult result = processPaymentUseCase.handle(command);
            PaymentResponse response = PaymentResponse.from(result);
            idempotencyManager.complete(key, HttpStatus.OK.value(), serialize(response));
            return ResponseEntity.ok(response);
        } catch (AccountNotFoundException ex) {
            return fail(key, HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex.getMessage());
        } catch (InsufficientFundsException ex) {
            return fail(key, HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS", ex.getMessage());
        } catch (InvalidCurrencyException ex) {
            return fail(key, HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_CURRENCY", ex.getMessage());
        } catch (InvalidTransferException ex) {
            return fail(key, HttpStatus.BAD_REQUEST, "INVALID_TRANSFER", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return fail(key, HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage());
        }
    }

    private ResponseEntity<ApiError> fail(IdempotencyKey key, HttpStatus status, String code, String message) {
        ApiError error = error(code, message);
        idempotencyManager.fail(key, status.value(), serialize(error));
        return ResponseEntity.status(status).body(error);
    }

    private ApiError error(String code, String message) {
        return new ApiError(code, message, MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    private String serialize(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize response", ex);
        }
    }

    private Currency resolveCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            return Currency.BRL;
        }
        try {
            return Currency.valueOf(currency.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidCurrencyException(currency);
        }
    }
}
