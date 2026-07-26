# Hydra Pay Monolith

Hydra Pay Monolith is the synchronous version of the payment engine POC. The HTTP request executes the full settlement path inside a single application and database transaction.

## Architecture
- Single Spring Boot service.
- PostgreSQL for accounts, transactions, ledger entries, and idempotency records.
- No messaging or background workers.

## Core flow (happy path)
```
Client
  -> Monolith (HTTP /payment)
     -> Idempotency evaluation
     -> Domain validation
     -> Double-entry ledger
     -> Transaction finalized
     -> Response returned
```

## Data model
- **accounts**: current balances.
- **transactions**: final settlement status (COMPLETED or FAILED).
- **ledger_entries**: immutable debit/credit entries.
- **idempotency_keys**: request hash + response snapshot + state.

## Idempotency
- `Idempotency-Key` is required.
- The system stores the request hash and response payload.
- Repeated calls with the same key return the stored response without reprocessing.
- Conflicts return 409 when the same key is reused with a different payload.

## Error handling
- Validation errors return 400/422 with structured error payloads.
- Insufficient funds returns 422.
- Idempotency conflicts return 409.

## Local run
```
./mvnw spring-boot:run
```
Defaults:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/hydra_monolith
SPRING_DATASOURCE_USERNAME=hydra
SPRING_DATASOURCE_PASSWORD=hydra
```

## Observability
- JSON structured logging (logstash encoder).
- Actuator endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.

