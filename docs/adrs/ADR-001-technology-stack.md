# ADR-001: Core Technology Stack

## Status
Accepted

## Context
The Hydra monolith must comply with the thesis constraints for Java 21, Spring Boot 3.x, and PostgreSQL.

## Decision
Adopt Java 21, Spring Boot 3.4.x, and PostgreSQL as the runtime stack, using Spring Web and Spring Data JPA for the API and persistence layers.

## Consequences
- The codebase uses Java records and virtual threads.
- PostgreSQL-specific SQL is used for schema initialization.
