# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Claude behavior

- After creating any new file, immediately run `git add <path>` for that file.
- The main branch is **`master`** (not `main`). Always commit to and create PRs against `master`.

## Commands

```bash
# Start PostgreSQL (required before running the app)
docker-compose up -d

# Run the application
./gradlew bootRun

# Build / test
./gradlew build
./gradlew test
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
