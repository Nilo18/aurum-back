# Aurum Backend

A Spring Boot REST API for an event and catering management application. Aurum supports public event bookings and feedback, alongside a staff workspace for managing clients, employees, events, menus, suppliers, vehicles, and inventory.

This repository contains the backend. The Angular frontend runs separately

## Features

- Event booking with email verification, menu selection, and server-calculated pricing.
- Staff authentication with passwords, email OTPs, and signed JWTs.
- Employee invitations and registration, with expiration checks and transactional single-use consumption.
- Searchable, paginated staff data and dashboard summaries.
- Feedback submission through expiring event invitations.
- Role-based access for `OWNER`, `ADMIN`, `STAFF`, and `DEMO`.
- SQL masking of client email/phone details and employee email addresses for demo access.
- IP rate limiting and per-token suspicious-activity detection.

The demo uses the shared application dataset, intended to contain synthetic business data. It is not an isolated database or sandbox.

## Technology

| Component | Technology |
| --- | --- |
| Language | Java 25 |
| Framework | Spring Boot 4.2.0-M1 |
| HTTP API | Spring MVC and Jakarta Validation |
| Authentication | Spring Security, BCrypt, JJWT |
| Persistence | Spring Data JDBC, JdbcClient, PostgreSQL |
| Database migrations | Flyway |
| Email | Spring Mail / SMTP |
| Rate limiting | Bucket4j |
| Tests | JUnit, Mockito, Spring Test / MockMvc |
| Build | Maven |

The Spring Boot version is a milestone release, as declared in [pom.xml](pom.xml).

## Run locally

### Requirements

- JDK 25, with `JAVA_HOME` configured.
- Maven, or the included Maven wrapper.
- A running PostgreSQL database; CI uses PostgreSQL 17.
- SMTP access for login codes, contact messages, invitations, and booking verification.

Run all commands from the repository root containing `pom.xml`.

### Configuration

Create a database named `aurum` and supply connection settings, SMTP credentials, and a JWT signing secret. Spring Boot reads these environment variables directly:

| Variable | Purpose |
| --- | --- |
| `SPRING_DATASOURCE_URL` | JDBC URL, such as `jdbc:postgresql://localhost:5432/aurum` |
| `SPRING_DATASOURCE_USERNAME` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `SPRING_MAIL_HOST` | SMTP host |
| `SPRING_MAIL_PORT` | SMTP port |
| `SPRING_MAIL_USERNAME` | SMTP username / configured owner email |
| `SPRING_MAIL_PASSWORD` | SMTP password |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | Enable authentication when required by the SMTP provider |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | Enable STARTTLS when required by the SMTP provider |
| `JWT_SECRET` | Base64-encoded random signing key with at least 32 decoded bytes |
| `JWT_EXPIRATION_MS` | Employee token lifetime; default `43200000` (12 hours) |

Example in PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://localhost:5432/aurum"
$env:SPRING_DATASOURCE_USERNAME = "aurum"
$env:SPRING_DATASOURCE_PASSWORD = "replace-with-your-local-password"
$env:SPRING_MAIL_HOST = "smtp.example.com"
$env:SPRING_MAIL_PORT = "587"
$env:SPRING_MAIL_USERNAME = "your-mailbox@example.com"
$env:SPRING_MAIL_PASSWORD = "replace-with-your-smtp-password"
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH = "true"
$env:SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE = "true"
$env:JWT_EXPIRATION_MS = "43200000"

# Generate once and retain the same key across application restarts.
$keyBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($keyBytes)
$rng.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($keyBytes)
```

On macOS/Linux, use the same environment variable names with `export`. A signing key can be generated with `openssl rand -base64 32`.

Alternatively, use a local `src/main/resources/application.properties` file. That file is excluded by `.gitignore`; never commit real credentials or signing keys. A `.env` file is not automatically loaded by this project.

### Start

```bash
mvn spring-boot:run
```

Wrapper alternatives:

```powershell
.\mvnw.cmd spring-boot:run
```

```bash
./mvnw spring-boot:run
```

The API runs at `http://localhost:8080` by default. Flyway applies the migrations in `src/main/resources/db/migration` during startup.

A quick connectivity check:

```bash
curl http://localhost:8080/
```

Expected response: `Hello world`.

## Authentication

For staff login:

1. Send `POST /api/auth/verify` with `{"email":"staff@example.com"}`.
2. Store the returned `transactionKey`; obtain the six-digit code from email.
3. Send `POST /api/auth/login` with the employee's credentials and code:

```json
{
  "email": "staff@example.com",
  "password": "employee-password",
  "role": "STAFF",
  "transactionKey": "key-from-verification-response",
  "otp": "code-from-email"
}
```

Use the returned token on protected requests:

```http
Authorization: Bearer <token>
```

Employee tokens expire after 12 hours by default. Demo tokens expire after one hour. There is no refresh-token flow; obtain a new token by logging in again.

Employee accounts are created through invitations. The application has no public owner-signup endpoint or documented default owner credentials; regular onboarding requires an existing owner account in your database.

For a quick demo, call `GET /api/auth/demo` and use the returned token with the same Bearer header.

## API access

These routes allow anonymous requests at the HTTP security layer:

| Method | Routes |
| --- | --- |
| GET | `/`, `/api/auth/demo`, `/api/menu/request-event` |
| POST | `/api/auth/verify`, `/api/auth/login` |
| POST | `/verify-contact-request`, `/contact` |
| POST | `/api/event/verify`, `/api/event` |
| POST | `/api/feedback`, `/api/feedback/verify-invitation` |
| POST | `/api/employee/validate-invite`, `/api/employee/register` |

Public access does not bypass payload, OTP, or invitation validation. Other requests require authentication. Controller-level `@PreAuthorize` rules further restrict staff operations; employee invitation and deletion require `OWNER`, while many management mutations allow `OWNER` or `ADMIN`.

| Resource | Base path |
| --- | --- |
| Authentication | `/api/auth` |
| Clients | `/api/client` |
| Employees | `/api/employee` |
| Events | `/api/event` |
| Menu | `/api/menu` |
| Feedback | `/api/feedback` |
| Dashboard | `/api/dashboard` |
| Products | `/api/product` |
| Suppliers | `/api/supplier` |
| Vehicles | `/api/vehicle` |

Controller mappings and request DTOs in `src/main/java/com/aurum/main` define the full request contracts.

## Verification and abuse controls

### OTPs

Codes are bound to an email address and purpose: login, contact, or event booking. They expire after five minutes. Every validation attempt consumes the challenge, including an incorrect attempt. Request another code after a failed attempt.

Verification responses contain only `transactionKey`; the OTP is delivered by email. Storage is capped at 10,000 challenges, with expired entries reclaimed during issuance.

### IP rate limits

The filter groups requests by client IP and operation:

| Group | Allowance |
| --- | --- |
| OTP delivery across all three verification routes | Capacity 10; refill 10 per minute |
| Staff login | Capacity 10; refill 10 per minute |
| Employee and feedback invitation checks | Capacity 30; refill 30 per minute |

Refills are gradual, so these are token-bucket limits rather than strict rolling-minute totals. Exhausted buckets return `429` with a `Retry-After` header. These are IP limits, not destination-email limits.

### Suspicious token activity

Each JWT has a unique ID. For a single token, more than 300 authenticated requests or 20 forbidden responses within its 60-second tracking window trigger revocation. Subsequent requests using that token return `401`; other login tokens remain valid.

| Property | Default |
| --- | --- |
| `security.token-activity.window-seconds` | `60` |
| `security.token-activity.max-requests` | `300` |
| `security.token-activity.max-denials` | `20` |
| `security.token-activity.capacity` | `10000` |

Activity detection is a heuristic, not proof of token theft. Tracking is bounded; if it cannot admit another token, authentication returns `503` instead of evicting an existing revocation.

OTPs, rate-limit buckets, and token activity are held in memory per backend instance. Restarting clears them, including revocations; a previously revoked token can then work until its original expiration. Multiple instances do not share this state.

Unexpected exceptions return a generic `500` response; details are logged server-side. Missing/invalid authentication returns `401`, and insufficient permission returns `403`.

## Tests and packaging

```bash
mvn test
mvn verify
mvn package
java -jar target/Aurum-Back-0.0.1-SNAPSHOT.jar
```

Tests cover controllers, service behavior, authorization, OTP binding and concurrency, JWT validation, invitation registration, and token revocation. Controller and service tests use mocked external dependencies; the application context smoke test also avoids a live database and SMTP server. These tests do not replace integration testing against PostgreSQL and your mail provider.

GitHub Actions runs `mvn -B verify` for pushes and pull requests targeting `master`, and supports manual runs. See [.github/workflows/ci.yml](.github/workflows/ci.yml).

## Project structure

```text
src/main/java/com/aurum/main/
  annotations/  Custom validation
  config/       Security and CORS configuration
  controller/   HTTP endpoints
  dto/          Requests and responses
  exception/    Application exceptions and HTTP error handlers
  filter/       JWT authentication and IP rate limiting
  model/        Database entities
  repository/   Persistence and custom SQL searches
  service/      Business logic, email, OTPs, and token activity
  utils/        Shared utilities
src/main/resources/db/migration/  Flyway migrations
src/test/                        Automated tests
```

## Deployment configuration

Use HTTPS and retain the signing secret securely across restarts. Configure trusted proxy forwarding so IP rate limits identify the intended client. Keep the frontend origins in `CorsConfig`, `EmployeeService`, and `EventService` aligned with your deployed frontend. Employee and feedback invitation links select localhost only for the exact `http://localhost:4200` request origin; all other origins, including an absent header, select the production frontend. Email sender addresses in the services also need to match your SMTP setup.

## Deploy to Render with Docker

Create a Render **Web Service** from this repository and select **Docker**. Use the repository root (the directory containing `pom.xml`) as the Docker build context and `./Dockerfile` as the Dockerfile path. Set the health check path to `/`.

The multi-stage image builds with Java 25 and Maven, then runs the packaged application as an unprivileged user on a Java 25 runtime. Tests are skipped during the image build; run `mvn verify` in CI before deploying.

The image defaults to the `production` profile and listens on `0.0.0.0` using Render's `PORT` environment variable, or port `8080` locally. No separate start command is needed.

Configure these variables in the Render dashboard:

| Variable | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `production` |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL (`jdbc:postgresql://...`) |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | Base64-encoded random signing key, at least 32 decoded bytes |
| `JWT_EXPIRATION_MS` | `43200000` |
| `SPRING_MAIL_HOST` | SMTP host, such as `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | SMTP port, such as `587` |
| `SPRING_MAIL_USERNAME` | SMTP username / owner email |
| `SPRING_MAIL_PASSWORD` | SMTP password or Gmail app password |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_AUTH` | `true` for authenticated SMTP |
| `SPRING_MAIL_PROPERTIES_MAIL_SMTP_STARTTLS_ENABLE` | `true` for STARTTLS on port 587 |

SMTP is used in every profile through `MailStrategy`. Your hosting provider must allow outbound connections to the configured SMTP port. Render free services block ports 25, 465, and 587, so this SMTP setup requires another suitable host or plan.

The Docker build context excludes the local `application.properties`, `.env` files, and common private-key files. Supply runtime configuration through Render environment variables.

To build and run locally with Docker:

```bash
docker build -t aurum-backend .
docker run --rm -p 8080:8080 --env-file .env aurum-backend
```

Create the `.env` file locally with the runtime variables above; do not commit it. The Docker command explicitly loads it at runtime.
