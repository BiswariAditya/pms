## PMS runbook (local + Docker)

### 1) DB issue (H2 vs Postgres)

**Goal**: use H2 for local dev, Postgres for Docker/production-like runs.

- **Local (H2)**:
  - `patient-service` uses profile `local`
  - H2 config is in `patient-service/src/main/resources/application-local.properties`
  - Seed data comes from `patient-service/src/main/resources/data.sql`

Run:

```bash
cd patient-service
mvn spring-boot:run
```

- **Docker (Postgres)**:
  - `patient-service` uses profile `docker`
  - Postgres config is in `patient-service/src/main/resources/application-docker.properties`
  - Docker Compose provides Postgres container (`postgres:15`)

Run via compose (see section 2).

If you ever want Postgres locally (without Docker), set:
- `SPRING_PROFILES_ACTIVE=docker`
- and point `spring.datasource.url` to your local Postgres instance.

### 2) Run the project (Docker Desktop or directly)

#### Option A — directly (no Docker)
Prereqs: Java 17 + Maven, and (optionally) Kafka on `localhost:9092`.

1) Start billing-service:

```bash
cd billing-service
mvn spring-boot:run
```

2) Start patient-service:

```bash
cd patient-service
mvn spring-boot:run
```

3) Swagger UIs:
- patient-service: `http://localhost:4001/swagger-ui/index.html`
- billing-service: `http://localhost:4002/swagger-ui/index.html`

> For local runs, billing-service gRPC is reached at `localhost:9001` (configured in `application-local.properties`).

#### Option B — Docker Desktop (recommended “one command”)
This is the practical “single container to run” experience: **one docker-compose stack**.

From repo root:

```bash
docker compose up --build
```

This starts:
- Postgres
- Kafka (KRaft)
- billing-service (REST+gRPC + Kafka consumer)
- patient-service (REST + DB + gRPC client + Kafka producer)

Ports on your machine:
- patient-service: `http://localhost:4001`
- billing-service: `http://localhost:4002`
- billing gRPC: `localhost:9001`
- postgres: `localhost:5432`
- kafka: `localhost:9092`

### 3) Swagger file for billing-service

Two options are now available:

- **Static OpenAPI file**: `billing-service/openapi.yaml`
- **Generated Swagger UI** (springdoc):
  - start billing-service
  - open `http://localhost:4002/swagger-ui/index.html`

### 4) Kafka setup (both microservices)

- **patient-service**:
  - Produces to topic `patient`
  - Payload is JSON (string)
  - Bootstrap:
    - local: `localhost:9092`
    - docker: `kafka:9092`

- **billing-service**:
  - Consumes topic `patient` via `@KafkaListener`
  - Logs received messages
  - Bootstrap:
    - local: `localhost:9092`
    - docker: `kafka:9092`

### 5) Notes about “single Docker container”

Running Postgres + Kafka + both microservices inside *one* Docker container is not recommended (you need a process supervisor, log management, and clean shutdown). The usual and correct approach is **one Docker Compose stack** (single command, multiple containers).

