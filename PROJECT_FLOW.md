# Patient Management System (PMS) — Project Flow & Features

This repository contains **two Spring Boot microservices**:

- `patient-service`: REST API + DB persistence for patients; triggers Billing via gRPC; publishes patient events to Kafka.
- `billing-service`: gRPC server that returns billing account information.

---

## Services, Ports, and Protocols

### `patient-service`
- **REST**: `server.port=4001`
- **Swagger UI** (springdoc): typically `http://localhost:4001/swagger-ui/index.html`
- **DB (default)**: H2 in-memory (`jdbc:h2:mem:patientdb`), H2 console path `/h2-console`
- **gRPC client → billing-service**:
  - host: `billing.grpc.host` (default intended for Docker DNS: `billing-service`)
  - port: `billing.grpc.port=9001`
- **Kafka producer**:
  - topic: `patient`
  - bootstrap servers: `spring.kafka.bootstrap-servers` (currently `localhost:9092`)

### `billing-service`
- **REST**: `server.port=4002` (no REST controllers currently; only gRPC is used)
- **gRPC server**: `grpc.server.port=9001`

---

## End-to-End Flow (Happy Path)

### 1) Create Patient (REST)
**Client** sends:
- `POST /patients` to `patient-service`
- JSON body: `PatientRequestDTO` (validated via Jakarta Validation)

**patient-service** does:
- Checks uniqueness of email
- Persists `Patient` via Spring Data JPA (`PatientRepository`)
- Calls **billing-service via gRPC** (RPC: `GetBillingInfo`)
- Publishes a **Kafka event** `PATIENT_CREATED` to topic `patient`
- Returns `PatientResponseDTO`

### 2) Billing account creation / retrieval (gRPC)
**patient-service** gRPC client calls:
- `BillingService/GetBillingInfo(BillingRequest) -> BillingResponse`

**billing-service** returns:
- `BillingResponse { accountId, status }`

### 3) Patient Event Publication (Kafka)
**patient-service** publishes:
- Topic: `patient`
- Payload: `patient.events.PatientEvent` serialized as `.toString()` (string form)

> Note: There is currently **no Kafka consumer** in this repo, so the event is produced but not handled downstream yet.

---

## REST API (patient-service)

Base path: `/patients`

- **GET `/patients`**
  - Returns all patients as `List<PatientResponseDTO>`

- **POST `/patients`**
  - Creates a patient
  - Side effects: gRPC call to billing-service + Kafka publish

- **PUT `/patients/{id}`**
  - Updates patient fields (with email uniqueness check)

- **DELETE `/patients/{id}`**
  - Deletes the patient

---

## Data Layer

### Current default (dev/test): H2
Configured in `patient-service/src/main/resources/application.properties` with:
- `spring.datasource.url=jdbc:h2:mem:patientdb`
- `spring.sql.init.mode=always` so `data.sql` seeds sample rows

### Postgres (planned)
There is a `patient-service/docker-compose.yml` that starts **Postgres**, but the application is not configured to use it by default.

To fully enable Postgres you typically add:
- a `spring.datasource.url=jdbc:postgresql://...`
- username/password
- and a separate profile (e.g. `application-docker.properties`)

---

## gRPC Contracts

### `billing_service.proto`
Shared (duplicated) proto exists in both services:

- package: `billing` (via `option java_package="billing";`)
- service: `BillingService`
- rpc: `GetBillingInfo(BillingRequest) returns (BillingResponse)`

### `patient_event.proto`
- package: `patient.events`
- message: `PatientEvent { patient_id, name, email, event_type }`

---

## What Was Broken / Incomplete (and fixed)

- **Java version mismatch**
  - Project was set to Java 21, but builds failed with: `release version 21 not supported`.
  - Updated both services to **Java 17** (compatible with Spring Boot 3).

- **gRPC wiring mismatch**
  - `billing-service` implemented `createBillingAccount(...)` but the proto defines `GetBillingInfo(...)`.
  - Updated `billing-service` gRPC implementation to override `getBillingInfo(...)`.

- **gRPC config keys mismatch**
  - `patient-service` config used `billing.grpc.host/billing.grpc.port` but the client read `billing.service.address/billing.service.port`.
  - Updated client to read `billing.grpc.host` + `billing.grpc.port`.

- **Incorrect cross-service Maven dependency**
  - `patient-service` depended on `billing-service` as a compile dependency (causes build/install coupling and is not needed since stubs are generated from proto).
  - Removed that Maven dependency from `patient-service/pom.xml`.

After these fixes:
- `billing-service`: `mvn test` passes
- `patient-service`: `mvn test` passes

---

## Remaining Steps to Fully “Complete” the Project

### Kafka (currently incomplete)
- **Missing infrastructure**: there is no Kafka broker in docker-compose.
- **Missing consumer**: no `@KafkaListener` exists in the repo.
- **Suggested completion**:
  - Add Kafka + Zookeeper (or KRaft) to a top-level `docker-compose.yml`
  - Add `spring.kafka.bootstrap-servers` for the target environment
  - Add a consumer service (new microservice) or add a consumer in an existing one
  - Decide event serialization (JSON/Avro/Protobuf bytes). Right now it publishes `patientEvent.toString()`.

### Docker orchestration
- Currently only Postgres compose exists (`patient-service/docker-compose.yml`) and doesn’t run the services.
- Suggested completion:
  - Add a **root** `docker-compose.yml` that runs:
    - `patient-service`
    - `billing-service`
    - `postgres`
    - `kafka`
  - Ensure `billing.grpc.host=billing-service` works inside the compose network.

### Database environment split (H2 vs Postgres)
- Decide the “real” DB for production (likely Postgres).
- Add a profile like:
  - `application-local.properties` (H2)
  - `application-docker.properties` (Postgres)

### Spring Boot version pinning
- Both services currently use `3.5.7-SNAPSHOT`.
- For stability, pin to a released Spring Boot version once you decide the target (avoid snapshot builds in production).

### Billing domain expansion (optional)
- Right now billing-service returns a hard-coded account id/status.
- Next steps:
  - Persist billing accounts in DB
  - Add idempotency keyed by `patientId`
  - Add more RPCs (e.g. `CreateBillingAccount`, `GetAccountBalance`, etc.)

---

## How to Run (Dev)

### Run billing-service (gRPC server)
From `billing-service/`:

```bash
mvn spring-boot:run
```

### Run patient-service (REST API)
From `patient-service/`:

```bash
mvn spring-boot:run
```

Then call `POST http://localhost:4001/patients`.

> If running without Docker, you likely want `billing.grpc.host=localhost` in `patient-service` so it can reach billing-service running on your machine.

