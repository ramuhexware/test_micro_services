# RapidX Microservices Suite

A robust, enterprise-grade Java/Spring Boot microservices architecture designed to manage user registration, authentication, counterparty accounts, history logging, and real-time audit logging with multi-database integration (PostgreSQL, MongoDB, IBM DB2) and ActiveMQ messaging.

---

## 🏗️ Architecture & Component Overview

The suite consists of **six Java Spring Boot microservices** and **four infrastructure datastores/message brokers**:

### Services

| Service Name | Port | Primary Database / Dependency | Purpose |
| :--- | :--- | :--- | :--- |
| **`auth-service`** | `8081` | PostgreSQL (`auth_db`) | Registers users, validates credentials, issues JWT tokens, and exposes user query filters. |
| **`account-service`** | `8080` | PostgreSQL (`acct_db`) | Manages Counterparty Account records. Connects to `email-service` for notifications and runs ActiveMQ messaging. |
| **`aggregator-service`** | `8082` | H2 (in-memory, local) | Acts as an orchestration gateway. Combines authentication validation and account operations into client-facing portal endpoints. |
| **`email-service`** | `8083` | None | Simulates email notification sending by logging events sent from other microservices. |
| **`audit-service`** | `8084` | MongoDB (`audit_db`) | Captures and persists logs for audit compliance. |
| **`history-service`** | `8085` | IBM DB2 (`histdb`) | Tracks historical records for system state transitions. |

### Infrastructure

*   **PostgreSQL** (Port `5433` on host, `5432` in Docker): Houses databases for authentication (`auth_db`) and accounts (`acct_db`).
*   **MongoDB** (Port `27017`): NoSQL store for auditing (`audit_db`).
*   **IBM DB2** (Port `50000`): Enterprise database for tracking history (`histdb`).
*   **Apache ActiveMQ** (Port `61616` TCP, `8161` Web Console): Handles asynchronous messaging between services.

---

## 🔌 API Endpoints Summary

Below are the primary HTTP REST routes exposed by the microservices:

### 1. Gateway Portal (`aggregator-service`)
*   `POST /api/portal/register` — Register a new user (delegates to auth-service).
*   `POST /api/portal/login` — Log in a user and retrieve a JWT token (delegates to auth-service).
*   `GET /api/portal/accounts/filter` — Filter user/counterparty accounts using dynamic JPA Specifications (requires JWT).
*   `GET /api/portal/accounts/update-family/{id}` — Sync account updates downstream to `account-service` (requires JWT).
*   `GET /api/portal/accounts/quick-search` — Performs a quick search on organizations/counterparty accounts (requires JWT).

### 2. Authentication (`auth-service`)
*   `POST /api/auth/register` — Creates a new user account.
*   `POST /api/auth/login` — Authenticates credentials and returns a JWT token.
*   `GET /api/auth/validate` — Validates the authorization header's JWT and returns user details.
*   `GET /api/auth/users/filter` — Admin query route using dynamic filters.

### 3. Accounts (`account-service`)
*   `GET /accounts` — List accounts (supports filtering by `orgId`, `roleStatus`, `lineOfBusiness`).
*   `POST /accounts/create` — Create a counterparty account (dispatches email alert via `email-service`).
*   `GET /accounts/filter` — Query accounts dynamically via JPA Specifications.
*   `GET /accounts/update-family/{id}` — Updates the account's counterparty name.
*   `GET /accounts/quick-search` — Queries the organizations search view.

### 4. Auditing (`audit-service`)
*   `POST /api/audit` — Append a new audit event log.
*   `GET /api/audit` — List all audit logs stored in MongoDB.

### 5. History (`history-service`)
*   `POST /api/history` — Create a state history record in DB2.
*   `GET /api/history` — Retrieve all history logs.

### 6. Email Sim (`email-service`)
*   `POST /api/notifications/email` — Accept and log mail dispatches.

---

## 📮 Request Body Payloads (for Postman)

When calling these endpoints, ensure you configure the request header: `Content-Type: application/json`.

### 1. User Registration
*   **Endpoints:** `POST http://localhost:8082/api/portal/register` (Gateway) or `POST http://localhost:8081/api/auth/register` (Direct)
*   **JSON Body:**
```json
{
  "username": "johndoe",
  "password": "securepassword123",
  "email": "johndoe@example.com",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

### 2. User Login / Authentication
*   **Endpoints:** `POST http://localhost:8082/api/portal/login` (Gateway) or `POST http://localhost:8081/api/auth/login` (Direct)
*   **JSON Body:**
```json
{
  "username": "johndoe",
  "password": "securepassword123"
}
```
*Note: The response will contain a JWT token. In subsequent requests to protected endpoints (e.g., Aggregator portal routes), pass this token in the header as: `Authorization: Bearer <your_jwt_token>`.*

### 3. Create Counterparty Account
*   **Endpoint:** `POST http://localhost:8080/accounts/create`
*   **JSON Body:**
```json
{
  "nameCntprtyAcct": "Acme Corp Ltd",
  "idOrgtn": 1001,
  "ucsCntPrtyAcctRoleSts": "ACTIVE",
  "ucsCntPrtyAcctRoleAssn": [
    {
      "uCSliofBusCd": {
        "id": 1,
        "liOfBus": "Commercial Banking"
      }
    }
  ]
}
```

### 4. Create Audit Log
*   **Endpoint:** `POST http://localhost:8084/api/audit`
*   **JSON Body:**
```json
{
  "action": "USER_LOGIN",
  "username": "johndoe",
  "details": "Successfully logged in from IP 192.168.1.50",
  "timestamp": "2026-06-17T15:30:00"
}
```
*Note: `timestamp` is optional. If not provided, the server defaults to the current system time.*

### 5. Create History Record
*   **Endpoint:** `POST http://localhost:8085/api/history`
*   **JSON Body:**
```json
{
  "eventType": "ACCOUNT_FAMILY_UPDATE",
  "payload": "{\"id\": 1, \"oldName\": \"Acme Corp\", \"newName\": \"Acme Corp Ltd\"}",
  "createdAt": "2026-06-17T15:30:00"
}
```
*Note: `createdAt` is optional. If not provided, the server defaults to the current system time.*

### 6. Send Email Notification (Simulated)
*   **Endpoint:** `POST http://localhost:8083/api/notifications/email`
*   **JSON Body:**
```json
{
  "recipient": "admin@rapidx.com",
  "subject": "System Warning",
  "body": "Connection load exceeded threshold limits."
}
```

---

## 🛠️ Prerequisites

Before getting started, make sure you have the following installed:
*   [Docker](https://www.docker.com/products/docker-desktop) and **Docker Compose**
*   [Java 17 JDK](https://adoptium.net/temurin/releases/?version=17) (only if running services locally outside Docker)
*   [Maven 3.x](https://maven.apache.org/download.cgi) (only if running services locally outside Docker)

---

## 🚀 How to Run the Application

### Option A: Running the Entire Suite via Docker Compose (Recommended)

This is the simplest option as it orchestrates all databases, brokers, and services in a virtual network.

1.  **Start all services and datastores:**
    ```bash
    docker compose up --build
    ```
    To run in detached (background) mode:
    ```bash
    docker compose up -d --build
    ```

2.  **Verify running containers:**
    ```bash
    docker compose ps
    ```

3.  **Shutdown all services:**
    ```bash
    docker compose down
    ```

---

### Option B: Running Microservices Locally (with Docker-managed Infrastructure)

If you are developing or debugging and want to run individual Spring Boot services locally on your machine, you can spin up the datastores via Docker first, then run each service via Maven.

1.  **Launch only the Databases and Infrastructure containers:**
    ```bash
    docker compose up -d postgres activemq mongodb db2
    ```

2.  **Verify databases are running and ports are bound:**
    *   PostgreSQL is exposed at port `5433` on the host.
    *   MongoDB is exposed at port `27017` on the host.
    *   IBM DB2 is exposed at port `50000` on the host.
    *   ActiveMQ console is accessible at `http://localhost:8161`.

3.  **Run Services:**
    Navigate into any service's subdirectory and launch it using Maven:
    ```bash
    cd <service-folder>
    mvn spring-boot:run
    ```

    > [!NOTE]  
    > Since PostgreSQL is exposed on host port `5433` (rather than container internal port `5432`), you may need to override the connection URLs via environment variables when starting services locally:
    > *   For `auth-service`: `DATABASE_URL=jdbc:postgresql://localhost:5433/auth_db mvn spring-boot:run`
    > *   For `account-service`: `DATABASE_URL=jdbc:postgresql://localhost:5433/acct_db mvn spring-boot:run`

---

## 📖 Swagger / OpenAPI Documentation

Each microservice includes Swagger UI for interactive API documentation and testing. Once services are running, access them at the following URLs:

*   **Auth Service API:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
*   **Account Service API:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **Aggregator Service API:** [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
