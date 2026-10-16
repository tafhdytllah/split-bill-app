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
The application will run on: http://localhost:4110

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

URL: `http://localhost:4110`

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

**Response** (`201 Created`)
```json
{
  "data": {
    "id": "43c1cad0-05ab-43cc-800f-4281e39f511f",
    "name": "Weekend Hangout",
    "participants": [
      {
        "id": "6a6c4020-f6ea-4c13-87fe-5aa07100b385",
        "name": "Andi"
      },
      {
        "id": "83ddd981-03cd-44d5-9fb0-c5b6644d1931",
        "name": "Budi"
      },
      {
        "id": "6243f843-1031-465c-b9ce-f54ef75f2c57",
        "name": "Cika"
      },
      {
        "id": "653a964a-ea89-49d7-9261-8008752d042c",
        "name": "Dodi"
      }
    ],
    "createdAt": "2026-10-05T03:04:30.200335400Z"
  }
}
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

**Response** (`201 Created`)
```json
{
  "data": {
    "id": "1e79edcf-032a-42ec-ac92-7c0a47b05809",
    "groupId": "43c1cad0-05ab-43cc-800f-4281e39f511f",
    "paidBy": "6a6c4020-f6ea-4c13-87fe-5aa07100b385",
    "amount": "200.00",
    "category": "FOOD",
    "expenseSplitType": "EQUAL",
    "splits": [
      {
        "id": "a441b728-3376-4f37-a783-7c18b5ac8e19",
        "participantId": "6a6c4020-f6ea-4c13-87fe-5aa07100b385",
        "name": "Andi",
        "amount": "50.00"
      },
      {
        "id": "3c952023-d1d8-44f5-b380-3e57002f5eb8",
        "participantId": "83ddd981-03cd-44d5-9fb0-c5b6644d1931",
        "name": "Budi",
        "amount": "50.00"
      },
      {
        "id": "ef56e346-21a2-45d1-af9a-cf8246533ad7",
        "participantId": "6243f843-1031-465c-b9ce-f54ef75f2c57",
        "name": "Cika",
        "amount": "50.00"
      },
      {
        "id": "147f5ee2-51c4-4e4c-9f4b-c20c95f1c6ff",
        "participantId": "653a964a-ea89-49d7-9261-8008752d042c",
        "name": "Dodi",
        "amount": "50.00"
      }
    ],
    "createdAt": "2026-10-05T03:06:19.537054800Z"
  }
}
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

**Response** (`201 Created`)
```json
{
  "data": {
    "id": "70ff38f2-fade-408d-8176-1775d26f971c",
    "groupId": "43c1cad0-05ab-43cc-800f-4281e39f511f",
    "paidBy": "6a6c4020-f6ea-4c13-87fe-5aa07100b385",
    "amount": "200.00",
    "category": "TRANSPORT",
    "expenseSplitType": "EXACT",
    "splits": [
      {
        "id": "335f0c54-9615-4b6c-bb22-d2ea66ac8949",
        "participantId": "6a6c4020-f6ea-4c13-87fe-5aa07100b385",
        "name": "Andi",
        "amount": "50.00"
      },
      {
        "id": "4d67095e-8960-4822-9b4e-2b9593231704",
        "participantId": "83ddd981-03cd-44d5-9fb0-c5b6644d1931",
        "name": "Budi",
        "amount": "25.00"
      },
      {
        "id": "db2c549b-96f4-4ac3-813b-b7afd8ac434d",
        "participantId": "6243f843-1031-465c-b9ce-f54ef75f2c57",
        "name": "Cika",
        "amount": "25.00"
      },
      {
        "id": "a0c0def7-1201-4f63-a922-0d508d78aead",
        "participantId": "653a964a-ea89-49d7-9261-8008752d042c",
        "name": "Dodi",
        "amount": "100.00"
      }
    ],
    "createdAt": "2026-10-05T04:29:50.754164Z"
  }
}
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

**Response** (`201 Created`)
```json
{
  "data": {
    "id": "fde506ca-af47-4d3f-993c-090e01c2112c",
    "groupId": "43c1cad0-05ab-43cc-800f-4281e39f511f",
    "paidBy": "6a6c4020-f6ea-4c13-87fe-5aa07100b385",
    "amount": "200.00",
    "category": "ACCOMMODATION",
    "expenseSplitType": "PERCENTAGE",
    "splits": [
      {
        "id": "3c9f226e-8cbd-4e59-a072-0a0cb12139fc",
        "participantId": "6a6c4020-f6ea-4c13-87fe-5aa07100b385",
        "name": "Andi",
        "amount": "80.00"
      },
      {
        "id": "60cd737f-de3c-4930-bebe-eae24f0f0997",
        "participantId": "83ddd981-03cd-44d5-9fb0-c5b6644d1931",
        "name": "Budi",
        "amount": "60.00"
      },
      {
        "id": "63643881-783f-4a33-b613-80b2db0ee5d0",
        "participantId": "6243f843-1031-465c-b9ce-f54ef75f2c57",
        "name": "Cika",
        "amount": "40.00"
      },
      {
        "id": "3454960d-6002-4a98-bd7f-1d10b4f827fe",
        "participantId": "653a964a-ea89-49d7-9261-8008752d042c",
        "name": "Dodi",
        "amount": "20.00"
      }
    ],
    "createdAt": "2026-10-05T04:38:16.949596100Z"
  }
}
```

---

### 3. Get Settlement

Get participant balances and optimized settlement transactions.

- GET /api/groups/{{groupId}}/settlement

```bash
curl http://localhost:4110/api/groups/{{groupId}}/settlement
```

**Response** (`200 OK`)
```json
{
  "data": {
    "groupId": "43c1cad0-05ab-43cc-800f-4281e39f511f",
    "balances": [
      {
        "participantId": "9c164dc8-efe2-43b0-bedb-280e69f68e3f",
        "amount": "-50"
      },
      {
        "participantId": "19f0bc53-6419-4506-a889-8bcdfb165f7c",
        "amount": "-50"
      },
      {
        "participantId": "51c5a151-6dd5-4b35-9337-d1f54223ddc8",
        "amount": "-50"
      },
      {
        "participantId": "ae53b5ad-97b1-4345-a430-a83f92d5101d",
        "amount": "150"
      }
    ],
    "settlements": [
      {
        "fromParticipantId": "9c164dc8-efe2-43b0-bedb-280e69f68e3f",
        "toParticipantId": "ae53b5ad-97b1-4345-a430-a83f92d5101d",
        "amount": "50.00"
      },
      {
        "fromParticipantId": "19f0bc53-6419-4506-a889-8bcdfb165f7c",
        "toParticipantId": "ae53b5ad-97b1-4345-a430-a83f92d5101d",
        "amount": "50.00"
      },
      {
        "fromParticipantId": "51c5a151-6dd5-4b35-9337-d1f54223ddc8",
        "toParticipantId": "ae53b5ad-97b1-4345-a430-a83f92d5101d",
        "amount": "50.00"
      }
    ]
  }
}
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

**Response** (`201 CREATED`)
```json
{
  "data": {
    "id": "7bcd7557-5457-4370-a05c-c0b8c90160a4",
    "groupId": "43c1cad0-05ab-43cc-800f-4281e39f511f",
    "fromParticipant": {
      "id": "19f0bc53-6419-4506-a889-8bcdfb165f7c",
      "name": "Cika"
    },
    "toParticipant": {
      "id": "ae53b5ad-97b1-4345-a430-a83f92d5101d",
      "name": "Andi"
    },
    "amount": "50.00",
    "createdAt": "2026-10-16T00:29:16.414818Z"
  }
}
```
---

### 5. Get Activity History

Get the activity history of a bill group.

- GET /api/groups/{{groupId}}/audit

```bash
curl http://localhost:4110/api/groups/{{groupId}}/audit
```

**Response** (`200 OK`)
```json
{
  "data": {
    "groupId": "43c1cad0-05ab-43cc-800f-4281e39f511f",
    "auditLogs": [
      {
        "id": "c42344dc-a498-457d-bc71-d51006f1115a",
        "action": "CREATED",
        "entityType": "PAYMENT",
        "entityId": "7bcd7557-5457-4370-a05c-c0b8c90160a4",
        "createdAt": "2026-10-16T00:29:16.440655Z"
      },
      {
        "id": "5921fa72-0fce-4b81-988a-b770bb654a68",
        "action": "CREATED",
        "entityType": "EXPENSE",
        "entityId": "2ef1cee6-c7b8-437d-b5fc-c922eb8a0057",
        "createdAt": "2026-10-16T00:23:35.364711Z"
      }
    ]
  }
}
```

---

## Example Workflow

The following workflow demonstrates a typical use case, from creating a group to recording a payment.

1. **Create a group** — Create a group and add its participants.
2. **Create an expense** — Record an expense, including the payer, amount, split type, and participants.
3. **View settlement** — Retrieve the group's balances and suggested transactions.
4. **Record a payment** — Record a payment from a debtor to a creditor.
5. **View activity history** — Retrieve the group's audit history to review recorded activities.

Use the IDs returned by each response in subsequent requests. The settlement endpoint calculates outstanding balances based on expenses and recorded payments.

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