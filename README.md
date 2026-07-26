# Hydra Pay Monolith

Hydra Pay Monolith is the synchronous implementation of the payment engine POC. Every request to `POST /payment` validates the idempotency key, verifies balances, and writes double-entry ledger records inside a single database transaction.

## Endpoint
`POST /payment`

**Header**
- `Idempotency-Key`: unique key per request

**Body**
```json
{
  "debitAccountId": "11111111-1111-1111-1111-111111111111",
  "creditAccountId": "22222222-2222-2222-2222-222222222222",
  "amount": 125.50,
  "currency": "BRL"
}
```

## Running Locally
Set PostgreSQL credentials via environment variables and run:
```
./mvnw spring-boot:run
```

Default configuration:
```
HYDRA_DB_URL=jdbc:postgresql://localhost:5432/hydra_monolith
HYDRA_DB_USER=hydra
HYDRA_DB_PASSWORD=hydra
```

The schema is initialized from `schema.sql` and two sample accounts are seeded in `data.sql`.
