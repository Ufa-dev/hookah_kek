# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Claude behavior

- The main branch is **`master`** (not `main`). Always commit to and create PRs against `master`.
- **Git operations are batched to the END of a task** — do NOT commit during development.
- **Never use `is2xxSuccessful`** in test assertions — always use precise status matchers: `isOk`, `isCreated`, `isNoContent`, `isNotFound`, `isBadRequest`, `isUnauthorized`.

## Development Workflow

**Approved plan → implement → review → commit (in that order)**

### 1. Implementation phase (no git)

- Write all files for the feature/fix without committing
- Run `./gradlew compileTestKotlin` first — fast compile check before the full test run
- Run tests scoped to the domain: `./gradlew test --tests "com.hookah.kek_hookah.feature.tobacco.e2e.<domain>.*"`
- Fix any production bugs found in the process — they go in the same commit as the tests

### 2. Code review pass (before committing)

After tests pass, review the implementation. Check:

- **401 tests do not create DB state** — use `UUID.randomUUID()` directly; no `randomUser()` or entity creation needed (auth check happens before any DB access)
- **List assertions are specific** — `slice.items.any { it.id == created.id }`, never just `isNotEmpty()`
- **Happy-path tests use `createXxxAndGet` helpers** — don't duplicate the deserialization inline
- **Status assertions are exact** — `isOk` (200), not `is2xxSuccessful`
- **`updatedBy` is asserted** in create/update happy-path tests
- **`updatedAt` advances** after update: `assertTrue(updated.updatedAt >= original.updatedAt)`
- **No dead helpers** — every extension function in `XxxExt.kt` must be used in at least one test
- **R2DBC value classes** — see Known Gotchas below

### 3. Commit phase

Only after tests pass and review is done:

```bash
git add <specific files — never git add -A>
git commit -m "type(scope): description"
```

## Git Workflow / Worktrees

- Worktrees создаются в `.worktrees/<branch-name>/` (НЕ в `.claude/worktrees/`).
- Имя ветки = имя задачи без префикса `worktree-`. Пример: `feature/add-market-page`, не `worktree-add-market-page`.
- `.worktrees/` добавлен в `.gitignore` — не коммитить его содержимое.

## Commands

```bash
# Start PostgreSQL (required before running the app)
docker-compose up -d

# Run the application
./gradlew bootRun

# Build / test
./gradlew build
./gradlew test

# Fast compile check (before running full tests)
./gradlew compileTestKotlin

# Run a single domain's tests
./gradlew test --tests "com.hookah.kek_hookah.feature.tobacco.e2e.<domain>.*"
```

Local dev uses `application-local.yaml`. PostgreSQL runs on port 6432 (mapped from 5432).

## Project purpose

**hookahPlace** is a hookah business management tool for tracking tobacco usage, purchasing analytics, and eliminating dead stock. The target user is a hookah bar operator tracking inventory by brand, flavor, and physical weight packs (контейнеры), with market pricing data via market_arc.

### Domain overview

| Domain | Table | Description |
|---|---|---|
| **brands** | `tabacoo_brand` | Tobacco brands (Darkside, Tangiers, etc.) with optional tags |
| **flavors** | `tabacoo_flavor` | Individual flavors per brand (name, strength 0–10) |
| **packs** (контейнеры) | `flavor_pack` | Physical containers: tracks `current_weight_grams` vs `total_weight_grams`; `id` is a user-defined label (VARCHAR 100), `flavor_id` is nullable |
| **tags** | `tags` | Shared label system attached to brands and flavors |
| **market_arc** | `market_arc` | Каталог рыночных SKU: конкретный продукт (бренд + вкус + граммовка + GTIN), доступный к закупке. Помогает оператору знать что именно искать при пополнении запасов. |

## Architecture

**Stack**: Kotlin + Spring Boot 4 (WebFlux/reactive) + R2DBC + PostgreSQL + Kotlin Coroutines + JWT auth

**API base path**: `/api/v1`

### Feature-based package structure

Each feature under `com.hookah.kek_hookah.feature/<feature>/` follows this layout:
```
feature/<feature>/
├── api/           # REST controllers + DTOs
├── internal/
│   ├── repository/  # R2DBC repositories
│   └── usecase/     # Commands (writes) and Queries (reads)
└── model/         # Domain models
```

### Current features
- **auth** — JWT login/register/refresh; tokens stored in `refresh_tokens` table
- **user** — User profile management
- **tobacco/brand** — Brand CRUD with tag associations
- **tobacco/flavor** — Flavors linked to brands (strength 0–10)
- **tobacco/pack** — Pack weight tracking (current vs total); `PackId` wraps `String` (not UUID) matching the `VARCHAR(100)` PK in `flavor_pack`
- **tags** — Shared tag system for brands and flavors
- **market** — Каталог рыночных позиций (SKU): бренд + вкус + название + вес + GTIN (штрих-код). Используется для поиска продукта при закупке. `/api/v1/market`
- **common** — Shared domain utilities and base types

### Infrastructure layer (`infrastructure/`)
- `SecurityConfig` — Spring Security + JWT filter for WebFlux
- `GlobalExceptionHandler` — centralized error handling
- `EventPublisher` — internal event system

### Database
- Migrations via **Flyway** in `src/main/resources/db/migration/`
- UUID primary keys; CITEXT for case-insensitive name fields
- `updated_at` auto-updated by a Postgres trigger
- OpenSearch 3.0 used alongside PostgreSQL (configured but check current usage)

## Frontend (`frontend/`)

React 18 + Vite SPA. Run separately from the Spring Boot backend.

```bash
cd frontend
npm install       # first time
npm run dev       # dev server on :3000 (proxies /api → :8080)
npm run build     # production build → frontend/dist/
npm run lint      # TypeScript type-check
```

**Stack**: React 18 + TypeScript + Vite + React Router v6 + TanStack Query v5 + Axios + Tailwind CSS v3 + shadcn/ui pattern (hand-rolled, `src/components/ui/`) + Sonner + Lucide React

### Key design decisions
- JWT tokens stored in `localStorage` (`kek_access`, `kek_refresh`). Axios interceptor in `src/lib/api.ts` handles auto-refresh — single in-flight promise prevents race conditions.
- `AuthContext` exposes `login`, `register`, `logout`; clears query cache on logout.
- `ProtectedRoute` wraps all protected routes; redirects to `/login` if unauthenticated.
- Vite proxies `/api/*` → `http://localhost:8080` (no CORS issues in dev).

### Route map
- `/login` → `LoginPage` (public)
- `/register` → `RegisterPage` (public)
- `/dashboard` → `DashboardPage` (protected)
- `/profile` → `ProfilePage` (protected)
- `/admin/brands` → `BrandsPage` — brand CRUD + tag management per brand
- `/admin/tags` → `TagsPage` — tag CRUD (create, rename, search)
- `/admin/packs` → `PacksPage` — pack (контейнер) CRUD with weight progress visualization
- `/admin/flavors` → `FlavorsPage` — CRUD вкусов с выбором бренда и управлением тегами
- `/admin/market` → `MarketPage` — каталог рыночных SKU с фильтрацией, сортировкой, CRUD

### Tag management gotcha
No `GET /tag` (list all) endpoint. Tags are derived from `GET /brand/brands?tags=` (brands with embedded tags). Search uses `GET /tag/name/{name}` (exact match only). To add a tag to a brand: search by exact name first; if not found, create it in TagsPage first.

### API type notes
Kotlin value classes (`BrandId`, `UserId`, `TagId`) serialize as plain UUID strings via Kotlin Jackson module. If API returns `{ "id": { "value": "uuid" } }`, update `src/types/index.ts`.

### Design system
"Кровавый уголь" — dark `#161616`/`#0f0f0f` backgrounds + gold (`#D4A647`) + crimson (`#9B2335`) accents; Playfair Display (headings) + Inter (body). Full tokens in `tailwind.config.ts`; safe-area vars + strength-slider CSS in `index.css`.

**Mobile**: use `page-root` + `page-container page-enter` wrappers; `min-h-dvh`; bottom tab bar (`md:hidden`); touch targets min 48px (`.touch-target`).

## Testing

All backend tests are **E2E integration tests** — full Spring context, real PostgreSQL via Testcontainers, real HTTP via `WebTestClient`. No mocking.

### Infrastructure (pre-existing, do not recreate)

| File | Purpose |
|------|---------|
| `support/IntegrationTest.kt` | Meta-annotation — apply to every test class |
| `support/IntegrationTestConfig.kt` | Singleton `postgres:16-alpine` Testcontainer; registers R2DBC + Flyway + JWT properties |
| `e2e/auth/AuthExt.kt` | `randomUser()`, `registerTestUser()`, `login()`, `refreshToken()` helpers |

### Before writing tests for a domain

Always read the controller first to verify:
- Exact route paths (`/id/{id}` vs `/{id}`, etc.)
- Return types (single vs `List<T>` vs `Slice<T>`)
- Which endpoints actually exist — don't add helpers for non-existent endpoints

### Location convention

```
src/test/kotlin/com/hookah/kek_hookah/feature/tobacco/e2e/<domain>/
├── <Domain>Ext.kt         # URL constant + WebTestClient extension functions
├── <Domain>CreateTest.kt  # POST happy path + validation
├── <Domain>GetTest.kt     # GET by id + list + 404
├── <Domain>UpdateTest.kt  # PUT/PATCH happy path + validation + edge cases
└── <Domain>DeleteTest.kt  # DELETE + 404
```

Package: `com.hookah.kek_hookah.feature.tobacco.e2e.<domain>`

### Step 1 — Create `<Domain>Ext.kt`

Extension file holds the base URL and typed helper functions. Authenticated calls extend `AuthorizedWebTestClient`.

```kotlin
package com.hookah.kek_hookah.feature.tobacco.e2e.<domain>

import com.hookah.kek_hookah.feature.tobacco.e2e.auth.AuthorizedWebTestClient
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

const val <DOMAIN>_URL = "/api/v1/<domain>"

fun AuthorizedWebTestClient.create<Domain>(request: <Domain>CreateDto): WebTestClient.ResponseSpec =
    post().uri(<DOMAIN>_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(request)
        .exchange()

fun AuthorizedWebTestClient.create<Domain>AndGet(request: <Domain>CreateDto): <DomainModel> =
    create<Domain>(request)
        .expectStatus().isOk
        .expectBody<<DomainModel>>()
        .returnResult().responseBody!!
```

### Step 2 — Write test classes

Every test class follows the same structure:

```kotlin
@IntegrationTest
class <Domain>CreateTest {

    @Autowired
    private lateinit var unauthorizedClient: WebTestClient

    @Test
    fun `should create <domain> successfully`() = runTest {
        val client = unauthorizedClient.randomUser()

        val result = client.create<Domain>AndGet(validRequest())

        assertAll(
            { assertNotNull(result.id) },
            { assertEquals(validRequest().name, result.name) },
            { assertNotNull(result.updatedBy) }   // always assert ownership
        )
    }

    @Test
    fun `should return 401 when unauthenticated`() = runTest {
        // No randomUser(), no DB state — auth check happens before any DB access
        unauthorizedClient.post()
            .uri(<DOMAIN>_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(mapOf("name" to "test"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `should return 400 for blank name`() = runTest {
        val client = unauthorizedClient.randomUser()
        client.create<Domain>(validRequest().copy(name = ""))
            .expectStatus().isBadRequest
    }
}
```

### Required test scenarios per domain

| Scenario | HTTP status | Test class |
|----------|-------------|------------|
| Happy path create | 200 | `CreateTest` |
| Unauthenticated → 401 (no DB state!) | 401 | `CreateTest` |
| Input validation (blank, too short) | 400 | `CreateTest` |
| Get by id (exists) | 200 | `GetTest` |
| Get by id (not found) | 404 | `GetTest` |
| List — verify specific item present | 200 | `GetTest` |
| Update happy path + `updatedAt` advances | 200 | `UpdateTest` |
| Update with invalid data | 400 | `UpdateTest` |
| Unauthenticated update → 401 (no DB state!) | 401 | `UpdateTest` |
| Delete (exists) → verify 404 after | 204 | `DeleteTest` |
| Unauthenticated delete → 401 (no DB state!) | 401 | `DeleteTest` |

### Assertion patterns

```kotlin
// Group assertions — shows ALL failures at once
assertAll(
    { assertNotNull(result.id) },
    { assertEquals("expected", result.name) },
    { assertNotNull(result.updatedBy) },
    { assertTrue(result.updatedAt >= original.updatedAt) }
)

// List assertion — SPECIFIC item, not just non-empty
assertTrue(slice.items.any { it.id == created.id })

// Temporal assertion
assertTrue(updated.updatedAt >= original.updatedAt)
```

### Authenticated vs unauthenticated calls

```kotlin
// Unauthenticated — use injected WebTestClient directly, NO randomUser(), NO entity creation
unauthorizedClient.delete()
    .uri("$DOMAIN_URL/${UUID.randomUUID()}")  // random UUID is enough — auth fails before DB
    .exchange()
    .expectStatus().isUnauthorized

// Authenticated — call .randomUser() to get AuthorizedWebTestClient with Bearer token
val client = unauthorizedClient.randomUser()
client.createBrand(...)
```

### Test isolation

- Each test creates its own user via `randomUser()` — no shared state between tests.
- The PostgreSQL container is a **singleton** — schema is created once by Flyway and shared across all tests. Tests that insert data do not clean up; use unique identifiers (UUID salt) to avoid conflicts.

### Known R2DBC gotchas

**Value class encoding in `.in()` queries:**

R2DBC cannot encode `@JvmInline value class` (e.g. `TagId`, `FlavorId`, `BrandId`) directly in `.in()`. Always unwrap to the raw UUID:

```kotlin
// WRONG — fails at runtime: "Cannot encode parameter of type TagId"
where("tag_id").`in`(tagIds)

// CORRECT
where("tag_id").`in`(tagIds.map { it.id })
```

**CITEXT columns are case-insensitive natively:**

`name` and `email` columns use PostgreSQL CITEXT — no need for `LOWER()` in R2DBC queries. Using `where("LOWER(name)")` generates invalid SQL (R2DBC table-qualifies the function call: `tabacoo_brand.LOWER(name)`).

```kotlin
// WRONG — generates invalid SQL
where("LOWER(name)").like("%${name.lowercase()}%")

// CORRECT — CITEXT handles case-insensitivity
where("name").like("%$name%")
```
