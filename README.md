# Reglog

Reglog is split into a React web client and two Spring Boot services backed by one MySQL database. Registration runs on `8081`; authentication and recovery run on `8082`. The original GitHub repositories supplied with the request were empty, so these deliverables are a fresh implementation and have not been pushed to GitHub.

## Build the two apps

Requirements: Node.js 20.19+ (or 22.12+) for the frontend; JDK 21 and Maven 3.9+ for the backend.

Frontend, from `ReglogFrontendApp`:

```powershell
npm ci
npm run build
npm run dev
```

Backend compile/package, from `ReglogBackendApp`:

```powershell
mvn -DskipTests package
```

## Run with your local MySQL on port 3306

1. In MySQL, create a database and a dedicated account (replace the sample password):

```sql
CREATE DATABASE reglog CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'reglog'@'localhost' IDENTIFIED BY 'choose-a-strong-password';
GRANT ALL PRIVILEGES ON reglog.* TO 'reglog'@'localhost';
```

2. In a PowerShell terminal, go to `ReglogBackendApp`, set the database configuration, and start registration:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/reglog?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USER = 'reglog'
$env:DB_PASSWORD = 'choose-a-strong-password'
$env:PORT = '8081'
mvn -f registration-service/pom.xml spring-boot:run
```

3. Open a second PowerShell terminal in `ReglogBackendApp`, set the same database values plus a fresh JWT signing key, then start authentication:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/reglog?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
$env:DB_USER = 'reglog'
$env:DB_PASSWORD = 'choose-a-strong-password'
$env:PORT = '8082'
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
mvn -f auth-service/pom.xml spring-boot:run
```

4. In a third terminal, go to `ReglogFrontendApp` and run `npm ci; npm run dev`. Open `http://localhost:5173`. Registration and sign-in work against those two local services. Configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, and `OTP_SENDER` in the auth terminal before starting it to enable recovery email. Password recovery requires a working SMTP server.

## Run locally with Docker


1. Copy `.env.example` to `.env` in this directory. Set strong database passwords and set `JWT_SECRET` to the output of `openssl rand -base64 32`.
2. Start MySQL, Mailpit, and both services: `docker compose up --build`. If another MySQL already uses host port 3306, set `MYSQL_PORT=3307` in `.env`; Compose MySQL still uses port 3306 internally.
3. In another terminal, go to `../ReglogFrontendApp`, copy `.env.example` to `.env`, run `npm install`, then `npm run dev`.
4. Open `http://localhost:5173`. Sign-up and sign in. Mailpit captures recovery mail at `http://localhost:8025`.

Docker Compose initializes schema through Hibernate (`ddl-auto=update`). `database/schema.sql` is the equivalent initial schema for managed MySQL deployments. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD` on both services. Configure `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD`, and `OTP_SENDER` on auth-service for real email delivery. Recovery is unavailable until SMTP is configured. Do not use the sample credentials or a development JWT key in production. Use HTTPS, a secret manager, a production mail provider, and database backups.

## Services and API

All request/response bodies are JSON. Passwords are only accepted over TLS in production and are stored as BCrypt hashes. Auth calls use `Authorization: Bearer <JWT>` where noted.

| Service | Port | Endpoint | Request | Notes |
|---|---:|---|---|---|
| registration | 8081 | `POST /api/registration/register` | `fullName`, `email`, `mobile`, `password` | public; returns 201 |
| auth | 8082 | `POST /api/auth/login` | `identifier`, `password` | public; identifier is email or mobile; returns JWT and user |
| auth | 8082 | `GET /api/auth/me` | — | bearer token required |
| auth | 8082 | `POST /api/auth/logout` | — | bearer token required; revokes its session |
| auth | 8082 | `POST /api/auth/forgot-password` | `identifier` | public; response is generic; OTP delivered to account email |
| auth | 8082 | `POST /api/auth/verify-otp` | `recoveryToken`, `otp` | public; 6 digits, 5 attempts, short expiry |
| auth | 8082 | `POST /api/auth/reset-password` | `resetToken`, `newPassword` | public, one-use recovery grant; revokes all sessions |
| auth | 8082 | `POST /api/auth/change-password` | `currentPassword`, `newPassword` | bearer token required; revokes all sessions |

Validation: full name 3–100 characters; unique valid email; unique 10-digit mobile; password 8–72 characters with uppercase, lowercase, digit, and special character. Confirm password is checked in the frontend and is not transmitted to the API.

## Security and data

- Auth tokens are signed JWTs with expiry and a session ID (`jti`). Each request checks signature, expiry, user binding, and the active session record in MySQL.
- Logout revokes the current session. Password change/reset revokes every active session for the user.
- Recovery OTPs are stored as BCrypt hashes, expire, have a five-attempt limit, and can only be verified once. The returned recovery handle does not reveal whether an account exists; unknown accounts receive a non-verifiable challenge.
- Registration has BCrypt strength 12 in auth-service verification and BCrypt hashing in registration-service. Both services point to the same database and user table.
- Browser token is held in localStorage for the requested simple client flow. For a public production deployment, prefer a same-site HttpOnly secure cookie and add CSRF protection.

## Postman

Import `postman/Reglog.postman_collection.json`. Set collection variable `registrationUrl` to `http://localhost:8081/api/registration`, `authUrl` to `http://localhost:8082/api/auth`; run register, login, then authorized requests. Copy the issued token into `token`. Recovery OTP can be read from Mailpit during local development.


