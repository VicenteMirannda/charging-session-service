# Charging Session Service

A Spring Boot microservice that simulates **electric vehicle (EV) charging sessions**.

It started as my part of a group project for the *Distributed Applications* course
(BSc in Computer Engineering, IPCB, 2024/2025). The team built an EV charging network
split into microservices: charging points (OPC), users and vehicles, energy suppliers and
billing (CEME), and this one, which handles charging sessions.
I later refactored it to improve code quality, error handling and test coverage.

## What it does

- **Starts a session**: checks that the charging point is available, gets the vehicle data,
  calculates the charging plan and marks the charging point as `IN_USE`.
- **Simulates the charge**: the charging power is the **lowest** of what the vehicle supports
  and what the charging point offers. Charging stops when the target battery level is reached.
- **Stops a session**: calculates the energy delivered from the elapsed time, frees the
  charging point and publishes a `SessionCompletedEvent` (used by billing to issue the invoice).
- **Session history** for each vehicle.

## Architecture

```
                       ┌──────────────────────────────┐
  HTTP (REST/JSON) ──► │   charging-session-service   │
                       │                              │
                       │  Controller  →  Service  →  Repository → H2 (JPA)
                       │                   │   │
                       │                   │   └─► SessionCompletedEvent ─► listener
                       └───────────────────┼──────────┘     (would go to billing via a broker)
                                           │ OpenFeign (HTTP)
                         ┌─────────────────┴──────────────┐
                         ▼                                ▼
               charging-point-service              vehicle-service
```

| Package      | Responsibility                                                        |
|--------------|-----------------------------------------------------------------------|
| `controller` | HTTP endpoints only. No business logic.                              |
| `service`    | Use cases (`ChargingSessionService`) and pure calculations (`ChargingSimulator`). |
| `model`      | JPA entity `ChargingSession`, which protects its own state rules.    |
| `repository` | Spring Data JPA access to the database.                              |
| `client`     | OpenFeign clients for the other microservices.                       |
| `event`      | Domain event published when a session ends, and its listener.         |
| `exception`  | Custom exceptions and the global handler that maps them to HTTP codes. |
| `config`     | Spring configuration (Feign clients, standalone fakes, `Clock`).     |

## API

| Method | Path                               | Description                   | Success |
|--------|------------------------------------|-------------------------------|---------|
| POST   | `/api/sessions`                    | Start a session               | 201     |
| POST   | `/api/sessions/{id}/stop`          | Stop a session                | 200     |
| GET    | `/api/sessions/{id}`               | Get one session               | 200     |
| GET    | `/api/sessions?vehicleId={id}`     | Session history of a vehicle  | 200     |

Errors use the standard [Problem Details](https://www.rfc-editor.org/rfc/rfc9457) format:
`400` invalid input, `404` not found, `409` charging point unavailable or session already
stopped, `502` a dependent service failed.

Example:

```bash
curl -X POST localhost:8012/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"vehicleId": 1, "chargingPointId": 1, "startBatteryPercent": 20, "targetBatteryPercent": 80}'
```

Interactive documentation (Swagger UI): http://localhost:8012/swagger-ui/index.html

## Running

Requirements: Java 17+. Maven is not needed (the project includes the Maven Wrapper).

```bash
./mvnw spring-boot:run
```

By default the service runs in **standalone mode**: the charging-point and vehicle services
are replaced by in-memory fakes, so it can be tried on its own.

| Charging point | Power  | Status    |   | Vehicle | Battery | Max power |
|----------------|--------|-----------|---|---------|---------|-----------|
| 1              | 50 kW  | AVAILABLE |   | 1 Tesla Model 3 | 60 kWh | 170 kW |
| 2              | 22 kW  | AVAILABLE |   | 2 Renault Zoe   | 52 kWh | 22 kW  |
| 3              | 150 kW | OFFLINE   |   |         |         |           |

To use the real services instead, run with any other profile and set their URLs:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod \
  -Dspring-boot.run.arguments="--clients.charging-point-service.url=http://host:8030 --clients.vehicle-service.url=http://host:8010"
```

With Docker:

```bash
docker build -t charging-session-service .
docker run -p 8012:8012 charging-session-service
```

## Tests

```bash
./mvnw test
```

- `ChargingSimulatorTest`: charging rules (power, energy, duration), plain JUnit.
- `ChargingSessionTest`: entity state rules (a session cannot be stopped twice).
- `ChargingSessionServiceTest`: use cases with Mockito mocks and a fixed `Clock`.
- `ChargingSessionControllerTest`: HTTP layer with `@WebMvcTest` (validation, status codes, JSON).
- `ChargingSessionApplicationTests`: the Spring context starts.

## Design decisions

- **Constructor injection** everywhere: dependencies are explicit, `final`, and easy to replace in tests.
- **DTOs (Java records) separate from the JPA entity**: the API contract does not change when the database model changes.
- **Business rules in one place**: `ChargingSimulator` holds pure calculations; the entity guards its own state (`complete()` fails if the session is not in progress).
- **Injected `Clock`**: time-dependent code is tested with a fixed time instead of `LocalDateTime.now()`.
- **`@TransactionalEventListener`**: the "session completed" reaction only runs after the transaction commits.
- **Global exception handler**: services throw meaningful exceptions; one class maps them to HTTP codes.

## Possible next steps

- Publish `SessionCompletedEvent` to a message broker (Kafka/RabbitMQ) so billing consumes it asynchronously.
- Service discovery with Eureka and resilience (retries/circuit breaker) on Feign calls.
- PostgreSQL with Flyway migrations instead of in-memory H2.
- Automatically complete sessions when the target battery level is reached (scheduled job).
