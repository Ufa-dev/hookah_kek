# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Start PostgreSQL (required before running the app)
docker-compose up -d

# Run the application
./gradlew bootRun

# Build
./gradlew build

# Run tests
./gradlew test
```

Local dev uses `application-local.yaml`. PostgreSQL runs on port 6432 (mapped from 5432).

## Project purpose

**hookahPlace** is a hookah business management tool for tracking tobacco usage, purchasing analytics, and eliminating dead stock. The target user is a hookah bar operator or enthusiast who tracks their tobacco inventory by brand, flavor, and physical weight packs (контейнеры). The system supports tagging brands/flavors for search/filtering, and will eventually incorporate market pricing data (market_arc).

### Domain overview

| Domain | Table | Description |
|---|---|---|
| **brands** | `tabacoo_brand` | Tobacco brands (Darkside, Tangiers, etc.) with optional tags |
| **flavors** | `tabacoo_flavor` | Individual flavors per brand (name, strength 0–10) |
| **packs** (контейнеры) | `flavor_pack` | Physical containers: tracks `current_weight_grams` vs `total_weight_grams`; `id` is a user-defined label (VARCHAR 100), `flavor_id` is nullable |
| **tags** | `tags` | Shared label system attached to brands and flavors |
| **market_arc** | `market_arc` | Market/purchase records per flavor (GTIN, weight, price tracking) — future |

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

### Commands

```bash
cd frontend
npm install       # first time
npm run dev       # dev server on :3000 (proxies /api → :8080)
npm run build     # production build → frontend/dist/
npm run lint      # TypeScript type-check
```

### Stack
- **React 18** + **TypeScript** + **Vite**
- **React Router v6** — client-side routing
- **TanStack Query v5** — server state, caching, mutations
- **Axios** — HTTP client with JWT interceptors (auto-refresh on 401)
- **Tailwind CSS v3** — utility styling with custom design tokens
- **shadcn/ui pattern** — hand-rolled components in `src/components/ui/` (no registry CLI used)
- **Sonner** — toast notifications
- **Lucide React** — icons

### Key design decisions
- JWT tokens stored in `localStorage` (`kek_access`, `kek_refresh`). The Axios interceptor in `src/lib/api.ts` handles automatic refresh — a single in-flight refresh promise is shared to prevent race conditions.
- `AuthContext` wraps the entire app and exposes `login`, `register`, `logout`. Query cache is cleared on logout.
- `ProtectedRoute` wraps all `/dashboard/*` routes; redirects to `/login` if unauthenticated.
- Vite dev server proxies `/api/*` to `http://localhost:8080`, so no CORS issues in development.

### Route map
- `/login` → `LoginPage` (public)
- `/register` → `RegisterPage` (public)
- `/dashboard` → `DashboardPage` (protected)
- `/profile` → `ProfilePage` (protected)
- `/admin/brands` → `BrandsPage` — brand CRUD + tag management per brand
- `/admin/tags` → `TagsPage` — tag CRUD (create, rename, search)
- `/admin/packs` → `PacksPage` — pack (контейнер) CRUD with weight progress visualization

### Tag management notes
The API has no "list all tags" endpoint (`GET /tag`). Tags are collected from `GET /brand/brands?tags=` (returns all brands with embedded tags). `TagsPage` derives its list from brands data. Searching uses `GET /tag/name/{name}` (exact match only). When adding a tag to a brand, user searches by exact name first; if not found, they're directed to create it in TagsPage first.

### API type notes
Kotlin value classes (`BrandId`, `UserId`, `TagId`) are expected to serialize as plain UUID strings via the Kotlin Jackson module. If the API returns `{ "id": { "value": "uuid" } }` instead, update `src/types/index.ts` accordingly.

### Design system — "hookahPlace / Midnight Lounge"
Custom Tailwind tokens in `tailwind.config.ts`. Key colors:
- `void` / `deep` / `surface` / `elevated` / `hover` — dark background scale (indigo-tinted midnight)
- `gold` / `gold-light` / `gold-dim` / `gold-glow` — primary accent (burnished gold `#D4A647`)
- `crimson` / `crimson-light` — secondary accent (deep red `#9B2335`)
- `ink` / `ink-dim` / `ink-muted` — text scale (warm cream white)
- Fonts: **Cinzel** (display headings) + **Outfit** (body/UI) — both from Google Fonts
- Grain texture overlay via `body::after` in `index.css`
- `--safe-bottom` / `--safe-top` CSS vars handle iOS notch & home bar via `env(safe-area-inset-*)`

### Mobile / responsive patterns
- `page-root` class applies top + bottom padding accounting for nav bar heights and safe areas
- Bottom tab bar (`md:hidden`) provides native-app-like navigation on iOS/Android
- Touch targets minimum `48px` enforced via `.touch-target` utility
- `min-h-dvh` used instead of `min-h-screen` for correct mobile viewport height
- `meta viewport-fit=cover` + `apple-mobile-web-app-capable` in `index.html`

### Adding a new page
1. Create `src/pages/YourPage.tsx`, wrap content with `<div className="page-root">` + `<div className="page-container page-enter">`
2. Add a `<Route>` in `src/App.tsx` inside the `AppLayout`
3. Add entry to `NAV_ITEMS` in `src/components/Navbar.tsx` (appears in both desktop nav and mobile bottom tabs)
4. Add API calls in `src/lib/api.ts` and types in `src/types/index.ts`
