# ADR-003: Synchronous Ledger Processing

## Status
Accepted

## Context
The monolith must execute the payment flow within a single HTTP request lifecycle.

## Decision
Process validation, balance checks, transaction creation, and double-entry ledger writes in a single database transaction, synchronously on the API request.

## Consequences
- Strong consistency (ACID) for each payment.
- The API returns the final status immediately.
