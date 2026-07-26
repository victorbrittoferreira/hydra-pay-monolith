package io.hydra.monolith.infrastructure.idempotency;

public record IdempotencyDecision(State state, Integer responseStatus, String responseBody) {
    public enum State {
        PROCEED,
        REPLAY,
        IN_PROGRESS
    }

    public static IdempotencyDecision proceed() {
        return new IdempotencyDecision(State.PROCEED, null, null);
    }

    public static IdempotencyDecision replay(Integer responseStatus, String responseBody) {
        return new IdempotencyDecision(State.REPLAY, responseStatus, responseBody);
    }

    public static IdempotencyDecision inProgress() {
        return new IdempotencyDecision(State.IN_PROGRESS, null, null);
    }
}
