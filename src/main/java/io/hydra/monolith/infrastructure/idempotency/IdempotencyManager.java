package io.hydra.monolith.infrastructure.idempotency;

import io.hydra.monolith.domain.IdempotencyKey;
import io.hydra.monolith.infrastructure.persistence.IdempotencyJpaRepository;
import io.hydra.monolith.infrastructure.persistence.IdempotencyRecordEntity;
import io.hydra.monolith.infrastructure.persistence.IdempotencyState;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class IdempotencyManager {
    private final IdempotencyJpaRepository idempotencyRepository;

    public IdempotencyManager(IdempotencyJpaRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    public IdempotencyDecision evaluate(IdempotencyKey key, String requestHash) {
        Optional<IdempotencyRecordEntity> existing = idempotencyRepository.findByIdempotencyKey(key.value());
        if (existing.isPresent()) {
            return resolveExisting(existing.get(), requestHash);
        }

        IdempotencyRecordEntity created = new IdempotencyRecordEntity(
                UUID.randomUUID(),
                key.value(),
                requestHash,
                IdempotencyState.IN_PROGRESS,
                Instant.now(),
                Instant.now()
        );

        try {
            idempotencyRepository.save(created);
            return IdempotencyDecision.proceed();
        } catch (DataIntegrityViolationException ex) {
            IdempotencyRecordEntity concurrent = idempotencyRepository.findByIdempotencyKey(key.value())
                    .orElseThrow(() -> ex);
            return resolveExisting(concurrent, requestHash);
        }
    }

    public void complete(IdempotencyKey key, int status, String responseBody) {
        updateRecord(key, status, responseBody, IdempotencyState.COMPLETED);
    }

    public void fail(IdempotencyKey key, int status, String responseBody) {
        updateRecord(key, status, responseBody, IdempotencyState.FAILED);
    }

    private IdempotencyDecision resolveExisting(IdempotencyRecordEntity record, String requestHash) {
        if (!record.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException("Idempotency key conflict");
        }
        if (record.getState() == IdempotencyState.IN_PROGRESS) {
            return IdempotencyDecision.inProgress();
        }
        return IdempotencyDecision.replay(record.getResponseStatus(), record.getResponseBody());
    }

    private void updateRecord(IdempotencyKey key, int status, String responseBody, IdempotencyState state) {
        IdempotencyRecordEntity record = idempotencyRepository.findByIdempotencyKey(key.value())
                .orElseThrow(() -> new IdempotencyConflictException("Idempotency key not found"));
        record.setResponseStatus(status);
        record.setResponseBody(responseBody);
        record.setState(state);
        record.setUpdatedAt(Instant.now());
        idempotencyRepository.save(record);
    }
}
