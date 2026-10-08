# Split Bill App

Split Bill App is a REST API for tracking shared expenses within a group and calculating settlements between participants.

---

## Tech Stack

- Java 21
- Maven
- Spring Boot
- PostgreSQL 17
- Flyway
- Docker
- GitHub Actions

---

## Features

- Bill group and participant management
- Expense tracking with equal, percentage, and exact splits
- Participant balance calculation
- Optimized settlement calculation
- Payment recording with debt validation
- Activity history

---

## Architecture

The application uses a feature-based layered architecture.

```text
src/main/java
└── com.tafhdev.split_bill_app
    ├── audit
    ├── expense
    ├── group
    ├── payment
    ├── settlement
    └── shared
```
Each feature follows the same layered structure with domain, repository, service, controller, and persistence layers.

---

## Requirements

- Java 21
- Docker
- Docker Compose

Maven installation is not required because the project includes Maven Wrapper.

---

## Running the Application


### Option 1: Run Locally

Start PostgreSQL:
```bash
docker compose up -d postgres
```

Run the Spring Boot application:
```bash
./mvnw spring-boot:run
```
The application will run on: http://localhost:4110

Stop PostgreSQL:
```bash
docker compose stop postgres
```

### Option 2: Run with Docker

Build the application image:
```bash
docker build -t split-bill-app .
```

Start the application and PostgreSQL:
```bash
docker compose up -d
```
the application will run on: http://localhost:4110

Stop the containers:
```bash
docker compose down
```

---

## Running Tests

Integration tests require PostgreSQL to be running.

Start PostgreSQL:
```bash
docker compose up -d postgres
```

Run all tests:
```bash
./mvnw clean test
```
The test suite includes unit tests and integration tests.

Stop PostgreSQL:
```bash
docker compose stop postgres
```

---

## API

URL: http://localhost:4110

### 1. Create Bill Group

Create a new bill group with its participants.

- POST /api/groups

```bash
curl -X POST http://localhost:4110/api/groups \
-H "Content-Type: application/json" \
-H "Idempotency-Key: group-001" \
-d '{
      "name": "Weekend Hangout",
      "participants": [
        "Andi",
        "Budi",
        "Cika",
        "Dodi"
      ]
    }'
```

---

### 2. Create Expense

#### Split Types

Create an expense using one of the supported split types:

- **EQUAL** — splits the expense equally among all selected participants.
- **EXACT** — assigns a specific amount to each participant.
- **PERCENTAGE** — splits the expense based on a percentage assigned to each participant.

Supported categories:

- FOOD
- TRANSPORT
- ACCOMMODATION


#### Equal Split

- POST /api/groups/{{groupId}}/expenses

```bash
curl -X POST http://localhost:4110/api/groups/{{groupId}}/expenses \
-H "Content-Type: application/json" \
-H "Idempotency-Key: expense-equal-001" \
-d '{
      "paidBy": "{{participant1Id}}",
      "amount": "200.00",
      "category": "FOOD",
      "split": {
          "type": "EQUAL",
          "participants": [
              {
                "participantId": "{{participant1Id}}"
              },
              {
                "participantId": "{{participant2Id}}"
              },
              {
                "participantId": "{{participant3Id}}"
              },
              {
                "participantId": "{{participant4Id}}"
              }
          ]
      }
    }'
```

#### Exact Split

- POST /api/groups/{{groupId}}/expenses

```bash
curl -X POST http://localhost:4110/api/groups/{{groupId}}/expenses \
-H "Content-Type: application/json" \
-H "Idempotency-Key: expense-exact-001" \
-d '{
      "paidBy": "{{participant1Id}}",
      "amount": "200.00",
      "category": "TRANSPORT",
      "split": {
        "type": "EXACT",
        "participants": [
          {
            "participantId": "{{participant1Id}}",
            "amount": "50"
          },
          {
            "participantId": "{{participant2Id}}",
            "amount": "25"
          },
          {
            "participantId": "{{participant3Id}}",
            "amount": "25"
          },
          {
            "participantId": "{{participant4Id}}",
            "amount": "100"
          }
        ]
      }
    }'
```

#### Percentage Split

- POST /api/groups/{{groupId}}/expenses

```bash
curl -X POST http://localhost:4110/api/groups/{{groupId}}/expenses \
-H "Content-Type: application/json" \
-H "Idempotency-Key: expense-percentage-001" \
-d '{
      "paidBy": "{{participant1Id}}",
      "amount": "200.00",
      "category": "ACCOMMODATION",
      "split": {
        "type": "PERCENTAGE",
        "participants": [
            {
              "participantId": "{{participant1Id}}",
              "percentage": "40.00"
            },
            {
              "participantId": "{{participant2Id}}",
              "percentage": "30.00"
            },
            {
              "participantId": "{{participant3Id}}",
              "percentage": "20.00"
            },
            {
              "participantId": "{{participant4Id}}",
              "percentage": "10.00"
            }
          ]
        }
      }'
```

---

### 3. Get Settlement

Get participant balances and optimized settlement transactions.

- GET /api/groups/{{groupId}}/settlement

```bash
curl http://localhost:4110/api/groups/{{groupId}}/settlement
```

---

### 4. Create Payment

Record a payment from one participant to another.

- POST /api/groups/{{groupId}}/payments

```bash
curl -X POST http://localhost:4110/api/groups/{{groupId}}/payments \
-H "Content-Type: application/json" \
-H "Idempotency-Key: payment-001" \
-d '{
      "fromParticipantId": "{{participant3Id}}",
      "toParticipantId": "{{participant1Id}}",
      "amount": "50.00"
    }'
```

---

### 5. Get Activity History

Get the activity history of a bill group.

- GET /api/groups/{{groupId}}/audit

```bash
curl http://localhost:4110/api/groups/{{groupId}}/audit
```

---

## Design Decision

### Settlement Optimization

The settlement calculation uses an optimization algorithm to reduce the number of payment transactions between participants.

For small groups, the application uses an exact backtracking approach to find a settlement with the minimum number of transactions. For larger groups, it falls back to a greedy approach to keep computation time predictable.

This approach balances settlement quality and performance while avoiding unnecessary complexity for typical bill-sharing scenarios.

---

### Money Handling

All monetary values are represented using `BigDecimal` and are stored with a scale of 2 decimal places.

The application uses a dedicated `Money` value object for monetary calculations to centralize validation, rounding, addition, subtraction, multiplication, and division.

Floating-point types such as `float` and `double` are not used for monetary values.

For example, splitting `100.00` equally between 3 participants results in:

```text
Participant 1: 33.33
Participant 2: 33.33
Participant 3: 33.34
Total:         100.00
```

The remaining fractional cent is assigned to one participant so that the calculated split always matches the original expense amount.

---

### Idempotency

Create operations support idempotency using the `Idempotency-Key` request header.

The key is scoped per operation type and stored together with a hash of the original request. If the same key is reused with the same request, the previously stored response is returned instead of creating a duplicate resource.

If the same key is reused with a different request, the API returns `409 Conflict`.

This prevents duplicate resources when clients retry requests due to network failures or timeouts.

---

## Contact
If you have any questions or comments about this project, please feel free to contact me at
- LinkedIn: [Taufik Hidayatullah](https://www.linkedin.com/in/tafhdytllah/)
- Email: [taufikhh.97@gmail.com](mailto:taufikhh.97@gmail.com)