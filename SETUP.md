# Wallet Module Milestones: Setup & Run Guide
### Roben Joseph B Tambayong - 2406453594

## Global Prerequisites

Please ensure the following tools are installed on your machine before running the milestones:

- **Java Development Kit (JDK) 17+**: Required for the Spring Boot backend.
- **PostgreSQL**: Required for the 75% milestone database connection.
- **Postman**: Required for testing the API endpoints.

---

## Milestone 25% (`feat/wallet-management`, Initial Core Logic)

**Associated Commits:**
- [`de31c6f`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/de31c6f) - *feat: implement initial full-stack integration feature*
- [`bf523f7`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/bf523f7) - *feat: implement wallet module with IDR support, audit trail, and unit tests* **(Run this commit for the 25% state)**

This milestone covers the foundational Wallet backend structure, including the initial IDR support, basic audit trail (history), and core unit tests. It utilizes the H2 in-memory database.

### How to Setup & Run

1. Checkout the final commit for this milestone:
   ```bash
   git checkout bf523f7
   ```

2. Start the Spring Boot backend from the root directory:
   ```bash
   ./gradlew bootRun
   ```

### Step-by-Step Postman Testing Guide (Milestone 25%)

Since the frontend was not implemented, you can test the core wallet logic directly via Postman.

**Step 1: Check Wallet Balance**
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/wallet`
- **Expected Response:** Returns the wallet object with available balance and escrow (held) balance for the default test user.

**Step 2: Top Up Wallet**
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/wallet/topup`
- **Body (raw JSON):**
  ```json
  {
    "amount": 500000
  }
  ```
- **Expected Response:** `200 OK` with a success message. You can hit the `GET` endpoint again to verify the balance increased.

---

## Milestone 50% (`feat/wallet-management`, Event Listeners & Basic Security)

**Associated Commits:**
- [`4f8864f`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/4f8864f) - *feat(wallet): integrate BidPlacedEvent listener and Spring Security auth for 50% milestone*
- [`bb60abe`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/bb60abe) - *test(wallet): add test coverage for hold funds logic and exception handling* **(Run this commit for the 50% state)**

This milestone introduces the `BidPlacedEvent` listener integration and initial Spring Security hooks for the wallet.

### How to Setup & Run

1. Checkout the final commit for this milestone:
   ```bash
   git checkout bb60abe
   ```

2. Start the Spring Boot backend:
   ```bash
   ./gradlew bootRun
   ```

### Step-by-Step Testing Guide (Milestone 50%)

Because Spring Security was introduced here, the endpoints are protected.

**Step 1: Bypass or Authenticate**
Depending on the exact security config at this commit, you may need to pass a Basic Auth header or a mock JWT token in Postman to access the endpoints.

**Step 2: Test Escrow Locking (Hold Funds)**
This milestone focuses on the logic that locks funds when a bid is placed.
- While you cannot easily trigger the `BidPlacedEvent` via a direct wallet API call, you can test the `holdFunds` logic by invoking the wallet service through the connected Auction API (if integrated at this stage) or by running the local unit tests to observe the event handling:
  ```bash
  ./gradlew test --tests "id.ac.ui.cs.advprog.bidmart.wallet.*"
  ```
- The tests will demonstrate 100% coverage of the exception handling and fund-locking mechanisms.

---

## Milestone 75% (`feat/auth+wallet`, Full Integration & DB)

**Associated Commits:**
- [`14a8f9a`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/14a8f9a) - *Merge branch 'feat/auth-and-management' into chore/wallet-auth-integration*
- [`3c0e7a6`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/3c0e7a6) - *Merge branch 'feat/wallet-management' into chore/wallet-auth-integration*
- [`94da612`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/94da612) - *feat: integrate auth and wallet modules with working escrow balance* **(Run this commit for the 75% state)**

This milestone represents the fully integrated state: the Wallet module working seamlessly with the Auth module, utilizing PostgreSQL for persistent transactional ledgers.

### Step 1: Setup PostgreSQL Database

1. Open pgAdmin or your PostgreSQL CLI.
2. Create a new database named `bidmart`:
   ```sql
   CREATE DATABASE bidmart;
   ```
*(Ensure your local `.env` or application properties match your local Postgres credentials).*

### Step 2: Initialize the Backend

1. Checkout the specific integration commit:
   ```bash
   git checkout 94da612
   ```

2. Boot the Spring Boot server. Hibernate/Flyway will initialize the `wallet` and `wallet_transaction` tables automatically:
   ```bash
   ./gradlew bootRun
   ```

### Step 3: API Testing Workflow

Because the `@RequiresPermission` annotations are now fully active, you must authenticate first.

**1. Login to get JWT Token:**
- **Method:** `POST` to your Auth login endpoint (e.g., `http://localhost:8080/api/auth/login`)
- **Body:** Valid user credentials.
- Copy the `Bearer` token from the response.

**2. Test Wallet Endpoints with Token:**
- In Postman, go to the **Authorization** tab, select **Bearer Token**, and paste your token.
- **Test History:** `GET http://localhost:8080/api/wallet/history`
  *Expected Output:* A JSON list of all `WalletTransaction` entities (e.g., `TOP_UP`, `HOLD`, `RELEASE`).
- **Test Withdrawal:** `POST http://localhost:8080/api/wallet/withdraw`
  ```json
  {
    "amount": 50000,
    "bankAccount": "BCA-12345"
  }
  ```
  *Expected Output:* `200 OK` showing successful deduction, or `400 Bad Request` if balance is insufficient (demonstrating the `try-catch` validation block).