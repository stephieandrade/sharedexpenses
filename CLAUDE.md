# CLAUDE.md — SharedExpenses

## Project description

SharedExpenses is a web application for managing shared expenses (Splitwise-style).
Users create groups (a trip, a shared flat, a dinner), add members and record expenses,
stating who paid and how the cost is split. The app calculates each member's balance and
suggests the **minimum number of payments** to settle the debts.

This is the Master's Final Project (TFM) for the Máster de Desarrollo con IA, BIG School.
Priorities: **SOLID, Docker, Spring Boot, Java 21 and free deployment (frontend on Vercel).**
A few well-finished features are better than many half-done ones.
The application does **not** integrate AI.

## Tech stack

Backend (`/backend`):
- Java 21
- Spring Boot 3.5 (Web, Data JPA, Security, Validation)
- Spring Security with JWT (`spring-boot-starter-oauth2-resource-server`, HMAC signature)
- `springdoc-openapi` to document the API (Swagger UI at `/swagger-ui.html`)
- PostgreSQL in production (Neon) and H2 for local development
- Flyway for the schema (`src/main/resources/db/migration`, `V<n>__description.sql`); Hibernate only
  validates (`ddl-auto: validate`). Every schema change is a new migration, never an edit of an applied one.
  The SQL must run on both PostgreSQL and H2.
- Maven (via the `./mvnw` wrapper)
- JUnit 5 + Mockito (+ MockMvc for controller tests)

Frontend (`/frontend`):
- React 19 + Vite 8, JavaScript (JSX), linted with oxlint (`npm run lint`)
- React Router 7 (`react-router` package)
- Native `fetch` to call the API (no axios)
- Simple, responsive hand-written CSS in `src/styles.css` with CSS variables and dark mode (no component libraries)
- Structure: `api/` (`client.js` = fetch wrapper that turns ProblemDetail into `ApiError`, `api.js` = one
  function per endpoint), `auth/` (session in localStorage, `AuthProvider`, `useAuth`, `RequireAuth`),
  `pages/`, `components/` (`group/` = the four tabs of a group and their forms), `hooks/`, `utils/`.
- A 401 with a token clears the session and returns to login. Client-side validation mirrors the backend
  rules, but the backend is the source of truth and its error messages are shown as they come.

Infrastructure:
- Docker (multi-stage backend Dockerfile) + docker-compose (backend + PostgreSQL) locally
- Backend deployed as a Docker container on Render
- Database on Neon (free PostgreSQL)
- Frontend deployed on Vercel

## Architecture: modular monolith with light hexagonal architecture

A single Spring Boot backend (monolith). No microservices, queues or event buses.
The code is organised by **business modules** and, inside each module, in hexagonal layers.

```
com.sharedexpenses
├── users/
│   ├── domain/          # Model and business rules. Plain Java, no Spring or JPA
│   ├── application/     # Use cases and ports (input and output interfaces)
│   └── infrastructure/  # Adapters: REST controllers, JPA persistence, mappers
├── groups/              # Groups and members (same structure)
├── expenses/            # Expenses and splits (same structure)
├── balances/            # Balance calculation and debt simplification (same structure)
└── shared/              # domain (Amounts, Guard, BusinessRuleException, NotFoundException, ForbiddenException), security, errors, config, web (health)
```

Mandatory rules:

1. `domain` **does not depend** on Spring, JPA or any other layer. Plain Java only.
2. `application` depends on `domain` and on **ports** (interfaces). Never on `infrastructure` classes.
3. `infrastructure` implements the ports (adapters). JPA entities live here and are mapped
   to/from domain objects; they are never exposed outside the adapter.
4. Controllers contain no logic: they validate input, call a use case and return a DTO.
5. The API never returns JPA entities or domain objects directly: always DTOs (records).
6. A module accesses another only through that module's public ports/use cases,
   never through its repositories or entities.

How it is wired (decided in step 3):
- Use cases are plain Java classes (no Spring annotations), created as `@Bean`s in each module's
  `infrastructure/<Module>Config`. Transactions live in the JPA adapters (`*PersistenceAdapter`);
  each use case does at most one write.
- Ports are named `XxxRepository` (application); Spring Data interfaces are `XxxJpaRepository`
  (package-private, infrastructure). Members and expense shares are `@ElementCollection`s.
- `GroupAccess.requireMember(groupId, userId)` is the single place that enforces group membership
  (`NotFoundException` → 404, `ForbiddenException` → 403). Every module calls it.
- `groups` gets a member's balance through its own `MemberBalanceProvider` port, implemented by `balances`
  (`MemberBalanceAdapter`), so the modules have no dependency cycle.
- `ArchitectureTest` fails the build if domain/application import Spring, Jakarta or infrastructure.
- Groups use optimistic locking (`version` column, `@Version` in `GroupEntity`, carried by the domain `Group`);
  a concurrent update raises `OptimisticLockingFailureException` (→ 409).
- The `Clock` bean uses `app.time-zone` (Europe/Madrid) for "today" defaults.

Principles the code must reflect (and that are explained in the README / Design Doc):
- **S**: one use case per class (`CreateExpenseUseCase`, `CalculateBalancesUseCase`...).
- **O / L**: ports with interchangeable adapters (e.g. JPA or in-memory persistence for tests);
  `SplitStrategy` implementations can be added without changing existing code.
- **I**: small, specific port interfaces.
- **D**: domain and application depend on abstractions, not on frameworks.
- **DRY, KISS, YAGNI**: no "just in case" abstractions. If something is not used, it is not created.

## Domain model

- **User**: id, name, email (unique), passwordHash (BCrypt)
- **Group**: id, name, description, currency (EUR by default), creatorId, createdAt, members, version
- **Member**: userId, joinedAt
- **Expense**: id, groupId, description, amount, paidById, date, splitType (EQUAL, AMOUNTS, PERCENTAGES), shares
- **ExpenseShare**: userId, amount
- **Payment**: id, groupId, fromUserId, toUserId, amount, date (records that a debt has been settled)
- Splits: `SplitStrategy` interface with `EqualSplit`, `AmountSplit`, `PercentageSplit`
- Balances: `BalanceCalculator`, `DebtSimplifier` → `SuggestedPayment`. Positive balance = the member is owed money

## Business rules

- Amounts always use `BigDecimal` with 2 decimals. Never `double`.
- The shares of an expense must add up **exactly** to the total. Shares are rounded down and the
  leftover cents are assigned deterministically, one each, to the first participants ordered by user id.
- In a PERCENTAGES split, the percentages must add up to 100.
- The payer and the participants must be members of the group.
- A member's balance = what they paid − their share + payments sent − payments received.
  The balances of a group always add up to 0.
- Debt simplification uses a greedy algorithm (biggest creditor with biggest debtor) and
  produces at most N−1 payments.
- A member with a non-zero balance cannot be removed.
- Permissions: any member can add members, create/delete expenses and register payments.
  Only the creator can remove other members; any member can leave the group themselves.
  The creator cannot be removed or leave.
- Balances list every current member plus former members who still have a non-zero balance.

## Planned use cases

- Users: `RegisterUser`, `Login` (returns a JWT)
- Groups: `CreateGroup`, `ListMyGroups`, `GetGroupDetail`, `AddMember` (by email), `RemoveMember`
- Expenses: `CreateExpense`, `ListExpenses`, `DeleteExpense`
- Balances: `CalculateBalances`, `GetSuggestedSettlement`, `RegisterPayment`

## REST API (prefix `/api`)

```
POST   /api/auth/register                         public, 201 {token, expiresAt, user}
POST   /api/auth/login                            public, 200 {token, expiresAt, user}
GET    /api/groups
POST   /api/groups                                201
GET    /api/groups/{groupId}                      detail with members
POST   /api/groups/{groupId}/members              201, body {email}
DELETE /api/groups/{groupId}/members/{memberId}   204
GET    /api/groups/{groupId}/expenses
POST   /api/groups/{groupId}/expenses             201
DELETE /api/groups/{groupId}/expenses/{expenseId} 204
GET    /api/groups/{groupId}/balances
GET    /api/groups/{groupId}/settlement
POST   /api/groups/{groupId}/payments             201
```

- Controllers live in `<module>/infrastructure/web` with their request/response records; responses
  map from domain objects with a static `from(...)`. The current user comes from `@CurrentUserId Long userId`.
- Every endpoint documented with OpenAPI (`@Tag`, `@Operation`, `@Schema` where useful), texts in Spanish.
- Uniform error format (Spring's `ProblemDetail`) from `ApiExceptionHandler`; 401/403 from Spring Security
  use the same format (`ProblemDetailSecurityHandler`). Status codes: 400 validation (`errors` map per field),
  401 unauthenticated / bad credentials, 403 not a member or not allowed, 404 not found,
  409 conflict (duplicate email, concurrent update), 422 business rule (`BusinessRuleException`), 500 unexpected.
- Validation messages use the fixed `es` locale (Hibernate Validator's Spanish messages).

## Security

- Stateless authentication with JWT. Passwords hashed with BCrypt (8–72 characters).
- JWTs are HS256, issued by `JwtTokenIssuer` (`sub` = user id, issuer `shared-expenses`, 24 h) and
  validated by Spring's resource server (`JwtConfig`).
- The JWT secret is **always** read from the `JWT_SECRET` environment variable (min. 32 bytes).
  Never write secrets in code or in versioned files. Without it, dev and the local docker-compose use a random
  key (`APP_JWT_ALLOW_RANDOM_SECRET=true`); the `prod` profile refuses to start.
- Only members of a group can view or modify its data.
- Rate limiting on auth (`AuthRateLimitFilter` + `FixedWindowRateLimiter`, in memory, no dependency): register
  5/hour per IP and 50/hour in total, login 10 per 5 minutes per IP → 429 ProblemDetail with `Retry-After`.
  Configurable with `RATE_LIMIT_REGISTER_PER_IP`, `RATE_LIMIT_REGISTER_TOTAL`, `RATE_LIMIT_LOGIN_PER_IP`.
  In `prod`, `server.forward-headers-strategy: native` takes the client IP from Render's proxy headers.
  In-memory counters assume a single instance (Render free plan).
- Registration captcha with Cloudflare Turnstile: the frontend sends the widget token in the `X-Captcha-Token`
  header; `RegistrationCaptchaFilter` (after the rate limit) checks it with `TurnstileCaptchaVerifier`
  (Spring `RestClient`, no dependency). Missing/invalid token → 400, Cloudflare unreachable → 503.
  Enabled only when `TURNSTILE_SECRET` is set (secret never in code); frontend uses `VITE_TURNSTILE_SITE_KEY`
  (public). The widget's allowed hostnames in Cloudflare must include `localhost` and the Vercel domain.
- CORS: allow only the frontend origin (`FRONTEND_URL`).
- There is no demo data in the code (no seed loader). The demo account and its sample groups are created by hand
  through the deployed app, and the data lives only in the Neon database. Never write demo credentials or sample
  data in code, README, frontend or tests: the repo is public and the credentials are sent privately in the TFM
  submission form.

## Configuration and profiles

- `dev` profile (default): in-memory H2.
- `prod` profile: PostgreSQL with `DB_URL`, `DB_USER`, `DB_PASSWORD` from environment variables.
- Backend variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `FRONTEND_URL` (comma-separated origins allowed),
  `TURNSTILE_SECRET`, `APP_JWT_ALLOW_RANDOM_SECRET`, rate limits
  (and `PORT`, set by Render).
- Frontend variables: `VITE_API_URL` (public backend URL), `VITE_TURNSTILE_SITE_KEY`.
- Provide `.env.example` (backend) and `frontend/.env.example` without real values.
- `.env` must be in `.gitignore`.

## Deployment

- **Backend**: Render, Docker web service built from `backend/Dockerfile`. `prod` profile.
  The free plan sleeps after inactivity (slow first request): document it in the README.
- **Database**: Neon (free PostgreSQL). The JDBC URL needs `?sslmode=require`.
- **Frontend**: Vercel, root directory `frontend`, command `npm run build`, output `dist`.
  Configure a rewrite to `index.html` so React Router works (`vercel.json`).

## Useful commands

```bash
# Backend
cd backend
./mvnw spring-boot:run      # Run locally (dev profile, H2)
./mvnw test                 # Run tests
./mvnw clean package        # Build the JAR

# Frontend
cd frontend
npm install
npm run dev                 # Vite dev server
npm run build               # Production build

# Everything with Docker
docker compose up --build   # Start backend + PostgreSQL
```

Local environment notes: `JAVA_HOME` must point to JDK 21. The Dockerfile build stage uses the
`maven:3.9-eclipse-temurin-21` image (the wrapper fails inside containers on the local old Docker Engine).

## Tests

- Mandatory unit tests (no Spring) for the domain: expense splitting (including cent
  rounding), balance calculation and debt simplification.
- Use case tests with Mockito on the ports.
- Controller tests with MockMvc for the main endpoints and security (access to other people's groups).
- Run `./mvnw test` after every relevant change and make sure it passes.

## Code conventions

- All code in English: packages, classes, methods, variables, comments, test names and API paths
  (`Group`, `Expense`, `Member`, `CreateExpenseUseCase`, `/api/groups`...).
- User-facing text stays in Spanish: error messages that reach the user (domain exceptions,
  validation) and all UI text. The README and TFM deliverables are written in Spanish.
- No comments in code or config files (no Javadoc, no `//`, no `package-info.java`): names must explain the code.
  Design explanations go in the README / Design Doc.
- Naming: use cases `XxxUseCase` (one public `execute` method); output ports named by role
  (`GroupRepository`, `MemberBalanceProvider`); adapters `XxxPersistenceAdapter` / `XxxAdapter`;
  Spring Data `XxxJpaRepository`; JPA classes `XxxEntity` / `XxxEmbeddable`; wiring `<Module>Config`.
  Cross-module query APIs that are not a single use case are named by role (`UserLookup`, `GroupAccess`).
- Use records for DTOs, input commands and value objects where it fits.
- Constructor injection, no field `@Autowired`.
- Input validation with Bean Validation (`@NotBlank`, `@Positive`, `@Size`, etc.).
- Simple, clean and responsive UI.

## Way of working

- Work in small steps. At the end of each step, summarise what was done and what is left.
- Do not add new dependencies or technologies without asking first.
- Do not over-engineer: no microservices, Kubernetes, queues or unnecessary abstractions.
- Do not initialise or push git until the user asks.
- Keep `README.md` up to date with the sections the TFM requires:
  description, stack, architecture (with diagram and SOLID / hexagonal justification),
  installation and running, structure, features, API documentation,
  test user, deployment URLs (frontend and backend), slides and video.

## Development plan

1. ✅ Repository structure (`backend/`, `frontend/`), base Spring Boot project and Dockerfile
2. ✅ Pure domain: groups, expenses, splits, balances and debt simplification + unit tests
3. ✅ Persistence (JPA adapters) and use cases
4. ✅ REST API, OpenAPI, error handling and security (registration, login, JWT, CORS, rate limiting, captcha) — steps 4 and 5 done together, because every endpoint needs the authenticated user
5. ✅ React frontend: login/registration, group list, detail with expenses, balances and settlement
6. docker-compose, deployment (Render + Neon + Vercel) and final README
