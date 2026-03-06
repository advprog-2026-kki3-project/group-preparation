# Step 1: Scope and Security Decisions (Authentication & User Management)

Date: 2026-03-06
Project: `group-preparation` (BidMart)
Status: Planning/architecture only. No feature code implemented in this step.

## 1. What was analyzed
- Inspected build and runtime baseline:
  - Spring Boot `3.5.10`
  - Java `21`
  - Spring Web + Thymeleaf
  - Spring Data JPA + H2 (current runtime DB)
- Verified there is no authentication/security foundation yet:
  - No Spring Security dependency configured
  - No JWT, password hashing, or 2FA integration configured
- Confirmed current codebase is still skeleton-level (home controller + dummy entity/repository).

## 2. Step 1 output: decisions to lock before implementation
These are recommended defaults so implementation can start with minimal ambiguity.

## 2.1 Required stack additions (Auth module baseline)
- `spring-boot-starter-security`: security filter chain + authentication pipeline.
- `spring-boot-starter-validation`: request DTO validation.
- `spring-boot-starter-mail`: email OTP delivery.
- JWT library: `com.auth0:java-jwt` (or `io.jsonwebtoken:jjwt-*`; pick one and keep it consistent).
- Password hasher: Argon2id via `Argon2PasswordEncoder`.
- 2FA TOTP: `com.warrenstrange:googleauth` (or equivalent RFC 6238 implementation).

Note: for production, H2 must be replaced by PostgreSQL/MySQL for session/token durability and admin runtime management.

## 2.2 Security defaults (recommended)
- Access token TTL: `15 minutes`
- Refresh token TTL: `30 days`
- Refresh rotation: `enabled` (rotate on every refresh)
- Refresh reuse detection: `enabled` (revoke token family on replay)
- Password hashing: `Argon2id` with Spring defaults tuned by load test
- 2FA methods phase 1:
  - `TOTP` (authenticator app)
  - `Email OTP` fallback
- Login attempt limit: `5 attempts / 15 minutes / account + IP`
- OTP attempt limit: `5 attempts / challenge`
- Max concurrent sessions per user (initial): `3`
- Concurrent session policy (initial): `revoke_oldest`

## 2.3 Token/session model decisions
- Access token is stateless JWT but must include `session_id` claim.
- Every authenticated login creates a server-side session record.
- Every refresh token is stored only as hash (never plaintext).
- Session revocation instantly blocks:
  - refresh operations
  - protected API access by session check middleware

## 3. SOLID-by-design boundaries (to prevent architecture drift)
Use these interfaces/services when coding starts. Keep one reason to change per class.

- `CredentialService`
  - responsibility: registration password policy + hash/verify
- `AuthenticationService`
  - responsibility: login flow orchestration (credentials -> 2FA gate -> token issue)
- `TwoFactorService`
  - responsibility: setup/verify/enable/disable 2FA methods
- `TokenService`
  - responsibility: JWT issue/parse/validate + refresh rotation rules
- `SessionService`
  - responsibility: create/list/revoke sessions + concurrent-session policy
- `AuthorizationService`
  - responsibility: permission evaluation for endpoint guards
- `RolePermissionService`
  - responsibility: runtime admin CRUD of roles/permissions assignments
- `AuditLogService`
  - responsibility: security event recording

SOLID constraints:
- S: no class handles both HTTP and business rules.
- O: add new 2FA methods via strategy interface, no login flow rewrite.
- L: each 2FA provider obeys same verification contract.
- I: small interfaces per responsibility (avoid "god service").
- D: controllers depend on interfaces, not concrete classes.

## 4. Code smell guardrails (explicitly banned)
- God classes (`AuthService` doing everything)
- Anemic validation (manual checks scattered in controllers)
- Hardcoded permission strings in many places (centralize constants/registry)
- Boolean parameter explosion (replace with value objects/policy objects)
- Duplicate auth checks in controllers (must use middleware/filter + method guards)
- Storing plaintext refresh tokens or OTP codes
- Static utility abuse for security-critical operations
- Time handling with `LocalDateTime.now()` directly in logic (inject `Clock`)

## 5. Configuration contract (externalized, not hardcoded)
Define typed config (e.g., `@ConfigurationProperties`) for:
- token TTLs
- max sessions
- session policy (`reject_new` or `revoke_oldest`)
- rate-limit thresholds
- OTP expiration window
- email sender identity

All values must be environment-overridable for runtime operations.

## 6. Risks discovered in Step 1
- Current H2 setup is insufficient for production-grade session/token revocation consistency.
- Email OTP introduces deliverability and latency risk; TOTP should be primary 2FA.
- Dynamic RBAC needs careful cache invalidation to reflect permission changes immediately.

## 7. Exit criteria for Step 1 (done)
- Stack baseline identified.
- Security defaults proposed with concrete values.
- SOLID service boundaries defined.
- Code smell prevention rules documented.
- Implementation explicitly deferred to Step 2+.

## 8. Confirmed decisions (locked on 2026-03-06)
1. Database target: `PostgreSQL`
2. JWT library: `JJWT`
3. Initial session policy: `revoke_oldest`
4. Backup recovery codes: `after core 2FA`

## 9. 25% milestone direction: functional authentication backbone
Goal for this milestone is a minimal, demonstrable auth vertical slice that is secure by default and extensible.

In-scope at 25%:
- register with email/password (validated + hashed)
- login with credential verification
- JWT access + refresh token issuance (via JJWT)
- refresh token rotation baseline
- security middleware for protected endpoint token verification
- foundation tables/entities for users/sessions/tokens (PostgreSQL)

Explicitly out-of-scope at 25%:
- full 2FA flows (only architecture hooks may be prepared)
- backup recovery codes
- dynamic RBAC admin runtime management UI/APIs
- advanced session listing/revocation UX
