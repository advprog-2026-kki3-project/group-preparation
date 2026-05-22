# Profiling

BidMart uses Java Flight Recorder (JFR) for JVM profiling. JFR is suitable for profiling
Spring Boot authentication flows because it captures CPU usage, memory allocation, garbage
collection, thread activity, exceptions, and method hot spots with low runtime overhead.

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
jcmd <PID> JFR.start name=bidmart-auth settings=profile filename=bidmart-auth-profile.jfr duration=60s
```

During the recording window, execute authentication flows:

- Register a new user through `POST /auth/register`
- Login successfully through `POST /auth/login`
- Attempt a failed login with an invalid password
- Refresh an access token through `POST /auth/refresh`
- Execute a 2FA verification flow when 2FA is enabled

If the recording was started without `duration`, stop it manually:

```powershell
jcmd <PID> JFR.stop name=bidmart-auth
```

Open the generated file in Java Mission Control:

```text
bidmart-auth-profile.jfr
```

## Heroku Profiling Option

JFR can also be enabled through a Heroku config var:

```powershell
heroku config:set JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=/tmp/bidmart.jfr,dumponexit=true,settings=profile,maxsize=50m,maxage=30m"
```

This is useful for short profiling sessions, but Heroku's filesystem is ephemeral. Download
or inspect the recording before the dyno restarts. For coursework evidence, local profiling
is usually easier to reproduce and screenshot.

To disable automatic JFR after profiling:

```powershell
heroku config:unset JAVA_TOOL_OPTIONS
```

## What To Analyze

Use Java Mission Control to inspect:

- Method profiling: identifies expensive authentication service methods.
- Allocation profiling: shows whether login, refresh, or 2FA flows allocate excessive memory.
- Garbage collection: checks whether auth traffic causes long GC pauses.
- Threads: checks whether requests block on database, mail, or token operations.
- Exceptions: identifies repeated failures such as invalid credentials or refresh token errors.

## Auth Profiling Scenario

Recommended scenario:

1. Start JFR for 60 seconds.
2. Register one user.
3. Run several successful login requests.
4. Run several failed login requests.
5. Refresh a token.
6. If available, run one 2FA challenge and verification flow.
7. Open the `.jfr` file in Java Mission Control.
8. Compare CPU hot spots, allocations, GC pauses, and thread blocking.

## Report Summary Template

```text
Profiling was implemented using Java Flight Recorder (JFR). A 60 second recording was
captured while exercising authentication flows: registration, successful login, failed login,
token refresh, and 2FA verification. The recording was analyzed using Java Mission Control.

The analysis focused on CPU hot methods, memory allocation, garbage collection pauses,
thread blocking, and exceptions. This is relevant because authentication is latency-sensitive
and security-sensitive. Slow password verification, excessive token allocation, database
blocking, or repeated exception-heavy paths would directly affect login reliability and user
experience.

Result summary:
- CPU: The hottest methods were BouncyCastle Argon2 password hashing methods, especially Argon2BytesGenerator.F and quarterRound. This is expected because authentication uses secure password verification, which is intentionally CPU-expensive.
- Memory allocation: Allocation samples were mostly related to file/classpath/string handling and small framework/runtime allocations. No continuously growing allocation pattern was observed during the recording.
- Garbage collection: Only one GC pause was recorded, lasting 7.19 ms. This indicates no significant GC pressure during the sampled authentication workload.
- Thread blocking: The longest monitor wait came from the JFR Recording Scheduler, not from application request handling. No request-thread deadlock or persistent contention was observed.
- Exceptions: A few JVM method-handle linkage events were recorded, but no repeated application-level authentication exception problem was identified beyond expected failed-login behavior.
- Follow-up action: Keep monitoring login failures, refresh token failures, and 2FA failures in Grafana. No immediate profiling optimization is required. Password hashing cost should only be tuned if login latency becomes too high under larger load.
```
