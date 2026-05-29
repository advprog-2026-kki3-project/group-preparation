<details>
<summary>Bidmart Project Milestones, Setup & Run Guide</summary>

## Global Prerequisites

Please ensure the following tools are installed on your machine before running any milestone:

- **Java Development Kit (JDK) 17+**, Required for the Spring Boot backend.
- **Node.js (v18+) & npm**, Required for the 75% milestone React frontend.
- **PostgreSQL**, Required for the 75% milestone database connection.
- **Postman**, Required for testing the API on the 25% and 50% milestones.

---

## Milestone 25% (`feat/auction`, Core Logic)

**Commit:** [`0dfb671b7701d4a5472d9e073d5929df64c3af07`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/0dfb671b7701d4a5472d9e073d5929df64c3af07)

This milestone covers the foundational backend structure. The frontend has not been implemented at this stage.

### How to Setup & Run

1. Checkout the specific commit for this milestone:

   ```bash
   git checkout 0dfb671b7701d4a5472d9e073d5929df64c3af07
   ```

2. Start the Spring Boot backend from the root directory:

   ```bash
   ./gradlew bootRun
   ```

3. Use Postman to hit the backend endpoints directly (default `http://localhost:8080`) to verify the auction creation and bidding logic.

### Step-by-Step Postman Testing Guide (Milestone 25%)

**Step 1, Start the Spring Boot Server**
Ensure the backend is running locally on port 8080 via `./gradlew bootRun`.

**Step 2, Open Postman and Create a New Request**
- Click the **"+"** button or **"New" > "HTTP Request"** in Postman.

**Step 3, Configure the Request**
- **Method:** Set the method to `GET` *(the controller uses `@GetMapping("/{auctionId}/bids")`)*
- **URL:** Enter the following:
  ```
  http://localhost:8080/api/auctions/1/bids
  ```
  > **Note:** Replace `1` with an actual `auctionId` that exists in the dummy repository. If no data has been seeded yet, the response will return an empty list `[]`.

**Step 4, Send the Request**
- Click the **"Send"** button.

**Step 5, Check the Response**
- Check the **Body** section at the bottom of Postman.
- A successful response will return a `200 OK` status.
- The method returns `ResponseEntity<List<BidResponseDTO>>`, so the expected output is a JSON array of bids, for example:

  ```json
  [
    {
      "bidId": "b1",
      "bidderName": "John Doe",
      "amount": 500.0,
      "timestamp": "2023-10-27T10:00:00"
    }
  ]
  ```

> This milestone relies on direct API testing since the frontend has not been built yet. The above is the intended method to verify the auction data at this stage.

---

## Milestone 50% (`feat/auction`, Event Listeners & Web Views)

**Commit:** [`1de2a5e616004fa5ebdabc4b2d4d0b9754f3015b`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/1de2a5e616004fa5ebdabc4b2d4d0b9754f3015b)

This milestone introduces event-driven backend logic along with Thymeleaf HTML templates. Item creation still relies on direct API calls as the Catalogue module integration is not yet complete.

### How to Setup & Run

1. Checkout the specific commit:

   ```bash
   git checkout 1de2a5e616004fa5ebdabc4b2d4d0b9754f3015b
   ```

2. Start the Spring Boot backend:

   ```bash
   ./gradlew bootRun
   ```

3. Use Postman to manually create an auction via the API, then verify the event listeners through the web view.

### Step-by-Step Postman & Web View Testing Guide (Milestone 50%)

**Step 1, Start the Spring Boot Server**
Open a terminal in the root directory and run:
```bash
./gradlew bootRun
```

**Step 2, Create an Auction via Postman**
Since the frontend form for creating an auction is not available at this stage, the auction must be created by calling the backend API directly.

- Open Postman and create a new **POST** request.
- **URL:** `http://localhost:8080/api/auctions`
- Go to the **Body** tab, select **raw**, and set the format to **JSON**.
- Use the following payload, which matches the `CreateAuctionRequestDTO`:

  ```json
  {
    "itemId": "item-123",
    "startingPrice": 100.0,
    "buyoutPrice": 1000.0,
    "endTime": "2026-12-31T23:59:59"
  }
  ```

- Click **Send**. The expected response is `200 OK` or `201 Created`. Please take note of the `auctionId` returned in the response body, as it is needed in the next step.

**Step 3, Test the Web View & Event Listeners via Browser**
With the auction now persisted in the backend, the Thymeleaf views and JavaScript event listeners can be verified.

- Open a web browser (Chrome, Firefox, etc.).
- Navigate to the auction detail page using the ID from the previous step:
  ```
  http://localhost:8080/auction/{auctionId}
  ```
  *(Replace `{auctionId}` with the actual ID from Step 2.)*

- The `auction/detail.html` Thymeleaf page should be rendered correctly.
- Place a bid using the UI. This will invoke the `bidding.js` script and fire the backend `BidPlacedEvent`, which can be used to confirm that the event listeners are functioning as expected.

---

## Milestone 75% (`feat/auction`, DB Integration & React Frontend)

**Commit:** [`a5367ca265649aa9f97de1586e39c35065249c23`](https://github.com/advprog-2026-kki3-project/group-preparation/commit/a5367ca265649aa9f97de1586e39c35065249c23)

This milestone fully integrates the Spring Boot backend with a PostgreSQL database and introduces the Vite + React frontend interface.

### Step 1: Setup PostgreSQL Database

1. Open pgAdmin or the PostgreSQL CLI (`psql`).
2. Create a new database named exactly `bidmart`:

   ```sql
   CREATE DATABASE bidmart;
   ```

### Step 2: Initialize the Backend (Terminal 1)

1. Checkout the specific commit:

   ```bash
   git checkout a5367ca265649aa9f97de1586e39c35065249c23
   ```

2. From the root directory, boot the Spring Boot server. This will also initialize the database tables automatically:

   ```bash
   ./gradlew bootRun
   ```

### Step 3: Boot the Frontend (Terminal 2)

1. Open a second terminal window and navigate to the frontend directory:

   ```bash
   cd frontend
   ```

2. Install all required Node modules:

   ```bash
   npm install
   ```

3. Start the Vite development server:

   ```bash
   npm run dev
   ```

4. Open a browser and navigate to the local URL provided by Vite (usually `http://localhost:5173`) to interact with the full application.

</details>
