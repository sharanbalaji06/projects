# Task Tracker (Spring Boot + Docker learning project)

A REST API for tracking tasks, with per-user accounts, categories, priorities/due dates, and search/filter/pagination. Built to practice Spring Boot, Spring Security (JWT), Docker, and CI/CD.

## Stack
- Java 17, Spring Boot 3 (Web, Data JPA, Validation, Security)
- JWT authentication (`jjwt`)
- PostgreSQL (via Docker Compose) / H2 (local `dev` profile, no Docker needed)
- Multi-stage Dockerfile (Maven build stage -> slim JRE runtime stage)
- GitHub Actions CI (build + test) and CD (Docker image published to GHCR on push to `main`)

## Run without Docker (H2 in-memory DB)
No local Maven install needed — use the included wrapper (auto-downloads Maven on first run):
```
# Windows PowerShell / cmd
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# macOS / Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
On Windows, keep the `-D...` flag quoted as shown — `mvn.cmd` can mis-split an unquoted flag that contains both `.` and `=`.

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

Set a real `JWT_SECRET` before running anywhere outside your laptop:
```
JWT_SECRET=$(openssl rand -base64 48) docker compose up --build
```

## Build and run the Docker image standalone
```
docker build -t task-tracker .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal -e DB_PORT=5432 \
  -e DB_NAME=tasktracker -e DB_USER=tasktracker -e DB_PASSWORD=tasktracker \
  -e JWT_SECRET=change-me \
  task-tracker
```

## Authentication
All `/api/tasks/**` and `/api/categories/**` endpoints require a `Bearer` JWT. Get one via register/login.

| Method | Path                 | Description               | Auth |
|--------|----------------------|----------------------------|------|
| POST   | /api/auth/register   | Create an account, returns a token | No |
| POST   | /api/auth/login      | Log in, returns a token    | No |

```
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"secret123\"}"
```
Response: `{"token": "...", "username": "alice"}`. Send that token as `Authorization: Bearer <token>` on subsequent requests.

## Task endpoints (scoped to the authenticated user)
| Method | Path                | Description                                               |
|--------|---------------------|-------------------------------------------------------------|
| GET    | /api/tasks          | List tasks (paginated). Filters: `title`, `completed`, `priority`, `categoryId`. Paging: `page`, `size`, `sort` |
| GET    | /api/tasks/overdue  | List incomplete tasks with a past `dueDate`                |
| GET    | /api/tasks/{id}     | Get one task                                                |
| POST   | /api/tasks          | Create a task                                               |
| PUT    | /api/tasks/{id}     | Update a task                                               |
| DELETE | /api/tasks/{id}     | Delete a task                                                |

Example body: `{"title": "Learn Docker", "completed": false, "priority": "HIGH", "dueDate": "2026-10-01", "categoryId": 1}`
(`priority` is one of `LOW`, `MEDIUM`, `HIGH`; `categoryId` and `dueDate` are optional.)

```
curl "http://localhost:8080/api/tasks?completed=false&priority=HIGH&page=0&size=10" \
  -H "Authorization: Bearer <token>"
```

## Category endpoints
| Method | Path                    | Description  |
|--------|-------------------------|--------------|
| GET    | /api/categories         | List all categories |
| GET    | /api/categories/{id}    | Get one category     |
| POST   | /api/categories         | Create a category    |
| PUT    | /api/categories/{id}    | Rename a category     |
| DELETE | /api/categories/{id}    | Delete a category     |

## Tests
```
./mvnw test
```
Integration tests spin up the full Spring context against an in-memory H2 database (`src/test/resources/application.yml`) — no Docker or Postgres needed.

## CI/CD
`.github/workflows/ci.yml` runs on every push and PR:
1. **build** — `./mvnw verify` (compiles and runs the test suite).
2. **publish** (only on push to `main`, after `build` passes) — builds the Docker image and pushes it to GitHub Container Registry as `ghcr.io/<owner>/<repo>:latest` and `:sha-<commit>`. Uses the built-in `GITHUB_TOKEN`, no extra secrets needed.

To pull the published image:
```
docker pull ghcr.io/<owner>/<repo>:latest
```
(Make the package public in the repo's GitHub Packages settings, or `docker login ghcr.io` first if it's private.)
