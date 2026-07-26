# ADR-002: Idempotency Storage

## Status
Accepted

## Context
Idempotency is mandatory to avoid double-spending and to guarantee deterministic responses.

## Decision
Persist idempotency keys in PostgreSQL with a request hash and a response snapshot (status + body). Replays return the stored response without re-executing the use case.

## Consequences
- The API can safely retry requests with the same idempotency key.
- Conflicting payloads for the same key return HTTP 409.
