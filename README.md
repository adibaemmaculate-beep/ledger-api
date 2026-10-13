# Ledger API

A small transaction ledger service: record credits and debits per account, reject overdrafts, and report balances and a running-balance history.

**Stack:** Java 21 · Spring Boot 3 (Web, Data JPA, Validation, Actuator) · H2 · JUnit 5 + Mockito · Docker · GitHub Actions CI

## Endpoints

| Method | Path | What it does |
| --- | --- | --- |
| `POST` | `/api/transactions` | Record a transaction. `201` on success, `422` if a debit would overdraw, `400` on invalid input |
| `GET` | `/api/transactions?accountId=ACC1` | History for an account, oldest first, each entry with its running balance |
| `GET` | `/api/accounts/{accountId}/balance` | Current balance |
| `GET` | `/actuator/health` | Health check used by Docker/Kubernetes |

Example request body:

```json
{ "accountId": "ACC1", "amount": "250.00", "type": "CREDIT", "description": "paycheck" }
```

## Run it

```bash
# With Maven (Java 21 required)
mvn test               # run all tests
mvn spring-boot:run    # start on http://localhost:8080

# With Docker
docker build -t ledger-api .
docker run -p 8080:8080 ledger-api
```

Try it:

```bash
curl -X POST localhost:8080/api/transactions -H "Content-Type: application/json" \
  -d '{"accountId":"ACC1","amount":"250.00","type":"CREDIT","description":"paycheck"}'
curl -X POST localhost:8080/api/transactions -H "Content-Type: application/json" \
  -d '{"accountId":"ACC1","amount":"75.25","type":"DEBIT","description":"groceries"}'
curl localhost:8080/api/accounts/ACC1/balance
curl "localhost:8080/api/transactions?accountId=ACC1"
```

## Design

```
HTTP  ->  LedgerController  ->  LedgerService  ->  TransactionRepository  ->  H2
          (parse/validate)     (business rules)     (Spring Data JPA)
                 \                                       
                  ApiExceptionHandler  (exceptions -> JSON errors)
```

- **Layered architecture.** The controller only handles HTTP, the service holds the business rules, the repository talks to the database. Each layer can be tested on its own.
- **Dependency injection.** Each class receives its dependencies through its constructor; Spring wires them together. That's why `LedgerServiceTest` can pass in a mock repository and run with no database.
- **Money as `BigDecimal`.** Floating point can't represent 0.10 exactly, which is unacceptable for money.
- **Balances are derived, not stored.** The ledger is append-only; a balance is the sum of entries, computed in the database. This keeps an audit trail and avoids a stored balance drifting out of sync.
- **DTOs separate from the entity.** `TransactionRequest`/`TransactionResponse` are the public API contract; the `Transaction` entity can change without breaking clients.
- **Validation at the edge.** Bean Validation rejects blank accounts, non-positive amounts, more than 2 decimal places, and unknown types before any business logic runs.
- **Atomic writes.** `@Transactional` makes the balance check and the insert one database transaction.

## CI/CD

`.github/workflows/ci.yml` runs on every push:

1. Builds the project and runs all unit and integration tests (`mvn verify`).
2. If they pass, builds the Docker image.
3. Starts the container and waits for `/actuator/health` to report `UP` (a smoke test).

## Known limitations and next steps

- **Concurrent debits.** Two simultaneous debits could both pass the balance check before either is saved. Fix: lock the account row (`SELECT ... FOR UPDATE` / pessimistic locking), use `SERIALIZABLE` isolation, or keep a versioned account row with optimistic locking.
- **Idempotency.** A client retrying after a timeout could record the same payment twice. Fix: accept an idempotency key and reject duplicates.
- **Persistence.** H2 is in-memory. For production, switch the datasource to PostgreSQL and manage schema changes with Flyway.
- **Deployment.** Add Kubernetes manifests (Deployment + Service) using `/actuator/health` for liveness and readiness probes.
- **Security.** No authentication yet; add Spring Security with OAuth2/JWT.
