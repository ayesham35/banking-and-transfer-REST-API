# Banking & Transfer REST API

A secure, production-style banking API built with Java 25, Spring Boot 4, and Spring Security 7. Authenticated users can open accounts, deposit and withdraw funds, and transfer money between accounts. All transactions are immutable and transfers are atomic: if any step fails, no money moves.

---

## Tech Stack

- **Java 25** / **Spring Boot 4.0.6**
- **Spring Security 7** with JWT authentication
- **Spring Data JPA** / **Hibernate 7**
- **H2** in-memory database (MySQL-swappable)
- **Lombok** for boilerplate reduction
- **jjwt 0.13.0** for JWT signing and verification
- **JUnit 6** / **Mockito** for testing

---

## Features

- JWT-based registration and login (carried over from Todo REST API)
- Open bank accounts with server-generated account numbers
- Deposit and withdraw from owned accounts
- Transfer money between any two accounts by account number
- View transaction history for owned accounts
- Transactions are immutable — no update or delete endpoints exist
- Atomic transfers with `@Transactional` — partial failures roll back completely
- Optimistic locking on accounts with `@Version` to handle concurrent requests
- Owner-scoped authorization: accessing another user's account returns 404, not 403

---
