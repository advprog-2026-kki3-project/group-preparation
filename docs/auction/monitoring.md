# Auction Monitoring

BidMart auction monitoring uses Spring Boot Actuator and Micrometer Prometheus.
The global application monitoring endpoints are already configured in
`src/main/resources/application.properties`.

## Endpoints

- `/actuator/health`: application health
- `/actuator/info`: application metadata
- `/actuator/prometheus`: Prometheus scrape endpoint

## Auction Metrics

- `bidmart_auction_created_total`
- `bidmart_auction_bid_success_total`
- `bidmart_auction_bid_failure_total`
- `bidmart_auction_bid_rejected_closed_total`
- `bidmart_auction_bid_rejected_too_low_total`
- `bidmart_auction_history_requests_total`
- `bidmart_auction_events_bid_published_total`

Metrics do not include auction ID, seller ID, bidder ID, listing ID, or bid
amount values. This keeps metric cardinality low and avoids exposing user data
through monitoring output.

## Verification

Run the application:

```powershell
.\gradlew.bat bootRun
```

Open the Prometheus endpoint:

```text
http://localhost:8080/actuator/prometheus
```

Perform auction flows, then search the Prometheus output for the metric names
above:

- Create an auction to increment `bidmart_auction_created_total`.
- Request bidding history to increment `bidmart_auction_history_requests_total`.
- Place a valid bid to increment `bidmart_auction_bid_success_total` and
  `bidmart_auction_events_bid_published_total`.
- Place a bid on a closed auction to increment
  `bidmart_auction_bid_failure_total` and
  `bidmart_auction_bid_rejected_closed_total`.
- Place a bid below the required minimum to increment
  `bidmart_auction_bid_failure_total` and
  `bidmart_auction_bid_rejected_too_low_total`.
