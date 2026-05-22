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

## Dashboard

Start the application first:

```powershell
.\gradlew.bat bootRun
```

Start Prometheus and Grafana:

```powershell
docker compose -f docker-compose.monitoring.yml up
```

Open Grafana:

```text
http://localhost:3000
```

Default credentials:

```text
admin / admin
```

Dashboard URL:

```text
http://localhost:3000/d/bidmart-auction/bidmart-auction-monitoring
```

Relevant widget URLs:

- Auction Creation: `http://localhost:3000/d/bidmart-auction/bidmart-auction-monitoring?viewPanel=1`
- Bid Outcomes: `http://localhost:3000/d/bidmart-auction/bidmart-auction-monitoring?viewPanel=2`
- Bid Rejections: `http://localhost:3000/d/bidmart-auction/bidmart-auction-monitoring?viewPanel=3`
- Auction Activity: `http://localhost:3000/d/bidmart-auction/bidmart-auction-monitoring?viewPanel=4`
- Application Scrape Health: `http://localhost:3000/d/bidmart-auction/bidmart-auction-monitoring?viewPanel=5`
