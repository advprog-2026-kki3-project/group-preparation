# Auction Profiling

BidMart uses Java Flight Recorder (JFR) for JVM profiling. JFR is suitable for
profiling Spring Boot auction flows because it captures CPU usage, memory
allocation, garbage collection, thread activity, exceptions, repository calls,
event publishing, and wallet interaction overhead with low runtime overhead.

## Local Profiling

Start the application:

```powershell
.\gradlew.bat bootRun
```

Find the Java process:

```powershell
jcmd
```

Start a 60 second profiling recording:

```powershell
jcmd <PID> JFR.start name=bidmart-auction settings=profile filename=bidmart-auction-profile.jfr duration=60s
```

During the recording window, execute auction flows:

- Create an auction through `POST /api/auctions`
- Fetch an auction by listing through `GET /api/auctions/listing/{listingId}`
- Fetch bidding history through `GET /api/auctions/{auctionId}/bids`
- Place valid bids through `POST /api/auctions/{auctionId}/bids`
- Attempt a bid below the required minimum
- Attempt a bid on an auction that is no longer accepting bids
- Fetch an ended auction to trigger settlement logic when applicable

If the recording was started without `duration`, stop it manually:

```powershell
jcmd <PID> JFR.stop name=bidmart-auction
```

Open the generated file in Java Mission Control:

```text
bidmart-auction-profile.jfr
```

## Heroku Profiling Option

JFR can also be enabled through a Heroku config var:

```powershell
heroku config:set JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=/tmp/bidmart-auction.jfr,dumponexit=true,settings=profile,maxsize=50m,maxage=30m"
```

This is useful for short profiling sessions, but Heroku's filesystem is
ephemeral. Download or inspect the recording before the dyno restarts. For
coursework evidence, local profiling is usually easier to reproduce and
screenshot.

To disable automatic JFR after profiling:

```powershell
heroku config:unset JAVA_TOOL_OPTIONS
```

## What To Analyze

Use Java Mission Control to inspect:

- Method profiling: identifies expensive auction service methods such as bid
  placement, auction lookup, and settlement.
- Allocation profiling: shows whether creating auctions, mapping bid history,
  or publishing bid events allocates excessive memory.
- Garbage collection: checks whether auction traffic causes long GC pauses.
- Threads: checks whether requests block on database access, wallet operations,
  or event listeners.
- Exceptions: identifies repeated rejected-bid paths, missing auctions, or
  failed settlement behavior.

## Auction Profiling Scenario

Recommended scenario:

1. Start JFR for 60 seconds.
2. Create one auction.
3. Fetch the auction by listing ID.
4. Place several successful bids.
5. Fetch bidding history several times.
6. Submit one bid below the required minimum.
7. Submit one bid against an auction that is closed or ended, if available.
8. Fetch an ended auction to trigger settlement logic, if available.
9. Open the `.jfr` file in Java Mission Control.
10. Compare CPU hot spots, allocations, GC pauses, thread blocking, and
    exception frequency.

## Report Summary Template

```text
Profiling was implemented using Java Flight Recorder (JFR). A 60 second
recording was captured while exercising auction flows: auction creation, auction
lookup by listing, bidding history retrieval, successful bid placement, rejected
low bids, rejected bids on closed auctions, and auction settlement when
available. The recording was analyzed using Java Mission Control.

The analysis focused on CPU hot methods, memory allocation, garbage collection
pauses, thread blocking, and exceptions. This is relevant because auction flows
are latency-sensitive and consistency-sensitive. Slow bid placement, excessive
bid-history allocation, blocking wallet operations, repository contention, or
exception-heavy rejected-bid paths would directly affect bidding reliability and
user experience.

Result summary:
- CPU: The recording lasted 527 seconds. JVM CPU usage stayed low during the
  sampled workload, with average JVM user CPU at 0.37% and average JVM system CPU
  at 0.70%. The hottest sampled methods were mostly JVM, JFR, file-system,
  classloading, and framework methods, including WinNTFileSystem calls, JFR
  periodic tasks, class definition, and PostgreSQL result processing. Auction
  service methods did not appear as dominant CPU hot spots in this recording.
- Memory allocation: Allocation samples were mostly related to Spring Boot
  DevTools file watching, classpath and classloading work, Jackson reflection,
  Hibernate query processing, Micrometer/Spring Security observation tags, and
  PostgreSQL result processing. No continuously growing allocation pattern was
  observed from the auction flow itself.
- Garbage collection: The recording did not include detailed GC pause events, so
  pause duration could not be measured from this file. G1 heap summaries were
  present, and heap usage returned after GC activity. Heap usage stayed within
  the committed heap and did not show an obvious runaway growth pattern during
  the sampled auction workload.
- Thread blocking: The recording did not include Java monitor wait or enter
  events, so no request-thread lock contention was visible from this file. No
  request-thread deadlock or persistent contention pattern was identified.
- Exceptions: No Java exception throw or exception statistics events were
  captured in the recording. Rejected bid paths did not appear as repeated
  exception-heavy behavior in the available JFR data.
- Follow-up action: Keep monitoring auction creation, bid success/failure, bid
  rejections, and auction activity in Grafana. No immediate profiling
  optimization is required. If future load tests show higher latency, profile
  with a shorter focused recording while disabling Spring Boot DevTools to reduce
  file watcher noise, then inspect repository queries, wallet interactions, bid
  history mapping, and settlement behavior.
```
