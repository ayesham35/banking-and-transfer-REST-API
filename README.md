# Banking API — Microservices Architecture

> **Branch:** `docker-microservices`  
> For the original monolithic version see the `master` branch.

This branch evolves the original monolith into a containerized two-service system. Authentication is handled by a separate `auth-service`; this service focuses exclusively on banking operations.

## What changed from master

- **Removed:** User entity, UserRepository, AuthController, AuthService, JwtService, JwtAuthenticationFilter
- **Added:** OAuth2 Resource Server config — tokens are validated via auth-service's JWKS endpoint
- Account ownership is now tracked by `owner_id` (Long) from the JWT subject, not a User foreign key
- MySQL replaces H2; Flyway manages schema migrations
- Dockerized with a multi-stage Dockerfile and docker-compose

## Running the full system

From the workspace root (parent of both `auth-service/` and `bankapi/`):

```bash
docker compose up --build
```

Services:
- auth-service: `http://localhost:8081`
- banking-service: `http://localhost:8080`
- Adminer (DB browser): `http://localhost:8090`

## Flow

1. Register at `POST http://localhost:8081/api/auth/register`
2. Use the returned JWT as a Bearer token for all banking endpoints
3. Banking-service validates the JWT against auth-service's JWKS endpoint

## Endpoints — Banking Service (port 8080)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/accounts | List my accounts |
| POST | /api/v1/accounts | Open an account |
| GET | /api/v1/accounts/{id} | Get account by ID |
| POST | /api/v1/accounts/{id}/deposits | Deposit funds |
| POST | /api/v1/accounts/{id}/withdrawals | Withdraw funds |
| POST | /api/v1/accounts/{id}/transfers | Transfer funds |
| GET | /api/v1/accounts/{id}/transactions | Transaction history |
| GET | /actuator/health | Health check |

## Tech Stack

- Java 25, Spring Boot 4.0.6
- Spring Security 7 with OAuth2 Resource Server
- Spring Data JPA + Flyway + MySQL 8
- Docker + docker-compose

## Architecture

```
Client → auth-service (port 8081) → issues RS256 JWT
Client → banking-service (port 8080) with JWT
banking-service → fetches public key from auth-service JWKS endpoint
banking-service → validates JWT signature → processes request
```

Two separate MySQL databases — auth-db and banking-db. No shared tables, no foreign keys across services. The only contract between services is the JWT format and the JWKS endpoint.

## Related

- [Auth Service](https://github.com/ayesham35/auth-service) — the identity provider that issues JWTs for this service
