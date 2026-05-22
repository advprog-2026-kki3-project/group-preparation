# Monitoring

BidMart exposes runtime monitoring through Spring Boot Actuator and Micrometer Prometheus.

## Endpoints

- `/actuator/health`: application health
- `/actuator/info`: application metadata
- `/actuator/prometheus`: Prometheus scrape endpoint
- `/actuator/metrics`: detailed metric lookup, protected by authentication

## Auth Metrics

- `bidmart_auth_registrations_total`
- `bidmart_auth_login_success_total`
- `bidmart_auth_login_failure_total`
- `bidmart_auth_login_rate_limited_total`
- `bidmart_auth_refresh_success_total`
- `bidmart_auth_refresh_failure_total`
- `bidmart_auth_refresh_replay_detected_total`
- `bidmart_auth_sessions_created_total`
- `bidmart_auth_sessions_revoked_total`
- `bidmart_auth_sessions_concurrent_policy_revoked_total`
- `bidmart_auth_sessions_limit_rejected_total`
- `bidmart_auth_2fa_login_challenges_total`
- `bidmart_auth_2fa_login_success_total`
- `bidmart_auth_2fa_challenges_created_total`
- `bidmart_auth_2fa_verify_success_total`
- `bidmart_auth_2fa_verify_failure_total`
- `bidmart_auth_2fa_attempt_limit_exceeded_total`
- `bidmart_auth_2fa_enabled_total`
- `bidmart_auth_2fa_disabled_total`

The 2FA challenge metric uses only low-cardinality enum tags: `purpose` and `method`.
Metrics do not include email, user ID, IP address, token, or OTP values.

## Verification

Run the application and open:

```text
http://localhost:8080/actuator/health
http://localhost:8080/actuator/prometheus
```

Perform register, login, refresh, and 2FA flows, then search the Prometheus output for
the metric names above.

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
http://localhost:3000/d/bidmart-auth/bidmart-auth-monitoring
```

Relevant widget URLs:

- Login Outcomes: `http://localhost:3000/d/bidmart-auth/bidmart-auth-monitoring?viewPanel=1`
- Registrations: `http://localhost:3000/d/bidmart-auth/bidmart-auth-monitoring?viewPanel=2`
- Refresh Token Outcomes: `http://localhost:3000/d/bidmart-auth/bidmart-auth-monitoring?viewPanel=3`
- Sessions: `http://localhost:3000/d/bidmart-auth/bidmart-auth-monitoring?viewPanel=4`
- Two-Factor Authentication: `http://localhost:3000/d/bidmart-auth/bidmart-auth-monitoring?viewPanel=5`
- Application Scrape Health: `http://localhost:3000/d/bidmart-auth/bidmart-auth-monitoring?viewPanel=6`
