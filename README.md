# Task Tracker (Spring Boot + Docker learning project)

A minimal REST API for tracking tasks. Built to practice Spring Boot, Docker, and Docker Compose.

## Stack
- Java 17, Spring Boot 3 (Web, Data JPA, Validation)
- PostgreSQL (via Docker Compose) / H2 (local `dev` profile, no Docker needed)
- Multi-stage Dockerfile (Maven build stage -> slim JRE runtime stage)

## Endpoints
| Method | Path              | Description       |
|--------|-------------------|--------------------|
| GET    | /api/tasks        | List all tasks     |
| GET    | /api/tasks/{id}   | Get one task       |
| POST   | /api/tasks        | Create a task      |
| PUT    | /api/tasks/{id}   | Update a task      |
| DELETE | /api/tasks/{id}   | Delete a task      |

Example body: `{"title": "Learn Docker", "completed": false}`

## Run without Docker (H2 in-memory DB)
```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
App starts on http://localhost:8080. H2 console at http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:tasktracker`).

## Run with Docker Compose (app + Postgres)
```
docker compose up --build
```
App: http://localhost:8080
Postgres: localhost:5432 (db `tasktracker`, user/pass `tasktracker`/`tasktracker`)

Stop and remove containers:
```
docker compose down
```
Add `-v` to also drop the Postgres data volume.

## Quick test
```
curl -X POST http://localhost:8080/api/tasks -H "Content-Type: application/json" -d "{\"title\":\"Learn Docker\",\"completed\":false}"
curl http://localhost:8080/api/tasks
```
