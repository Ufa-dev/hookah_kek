# Flavors UI + Market UI + Pack Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Добавить UI для вкусов (Flavors) с выбором бренда и тегами, исправить форму пака (dropdown вкусов), построить страницу Market с фильтрацией/сортировкой/infinite scroll, а также дополнить бэкенд недостающими эндпоинтами и добавить контроллер для market.

**Architecture:** Бэкенд — Spring Boot WebFlux/R2DBC, feature-based packages. Фронтенд — React 18 + TanStack Query v5 + Tailwind CSS (дизайн-система "Midnight Lounge"). Все новые API следуют cursor-based pagination (header `X-Next-Cursor`). Фронтенд читает cursor из response header через axios и передаёт как query-param `cursor` в следующий запрос.

**Tech Stack:** Kotlin + Spring WebFlux + R2DBC + PostgreSQL | React 18 + TypeScript + TanStack Query v5 + Axios + Tailwind CSS v3 + Lucide React + Sonner

---

## Контекст проекта (читай перед стартом)

### Структура пакетов (бэкенд)
```
src/main/kotlin/com/hookah/kek_hookah/feature/
├── tobacco/
│   ├── brand/         # BrandService, TabacoBrandController → /api/v1/brand
│   ├── flavor/        # FlavorService, FlavorController → /api/v1/flavor
│   └── pack/          # PackService, PackController → /api/v1/pack
├── market/            # ТОЛЬКО команды/репозиторий — нет сервиса и контроллера
├── tags/              # TagService → /api/v1/tag
├── auth/              # JWT
├── user/              # /api/v1/users
└── common/            # shared utilities
```

### Паттерн feature (бэкенд)
- `api/` — REST controllers + DTOs
- `internal/repository/` — R2DBC repositories
- `internal/usecase/` — Commands (writes) и Queries (reads), каждый в отдельном файле
- `model/` — domain models, events, value classes

### Паттерн событий (Events)
Все команды публикуют события через `EventPublisher`:
```kotlin
eventPublisher + SomeCreatedEvent(entity = saved, publishedAt = OffsetDateTime.now())
```
`EventPublisher` — infrastructure bean, инжектится в usecase commands.

### Паттерн репозитория (cursor pagination)
Cursor = UUID последнего элемента, `WHERE id > :cursor ORDER BY id ASC LIMIT :limit`.
Контроллер кладёт следующий cursor в header `X-Next-Cursor`.

### Фронтенд структура
```
frontend/src/
├── pages/admin/       # BrandsPage.tsx, PacksPage.tsx, TagsPage.tsx
├── lib/api.ts         # все HTTP вызовы, axios с JWT interceptor
├── types/index.ts     # все TypeScript типы
├── App.tsx            # роуты
└── components/Navbar.tsx  # NAV_ITEMS
```

### Дизайн-система (фронтенд)
- Tailwind токены в `tailwind.config.ts`: `void`, `deep`, `surface`, `elevated`, `hover` (dark backgrounds), `gold`/`gold-light` (акцент #D4A647), `crimson` (глубокий красный #9B2335), `ink`/`ink-dim`/`ink-muted` (текст)
- Fonts: `font-display` = Cinzel (заголовки), `font-body` = Outfit (UI)
- Grain overlay в `index.css`
- Каждая страница оборачивается: `<div className="page-root"><div className="page-container page-enter">`
- Смотри `BrandsPage.tsx` как эталон для карточек, тегов, форм, infinite scroll
- Смотри `PacksPage.tsx` как эталон для delete dialog и weight progress

### Существующие API эндпоинты (бэкенд)
```
POST   /api/v1/flavor                    → create
PUT    /api/v1/flavor/{id}               → update
PATCH  /api/v1/flavor/add-tag            → addTag
PATCH  /api/v1/flavor/remove-tag         → removeTag
GET    /api/v1/flavor/flavors?tags=...   → findByTags (X-Next-Cursor header)
GET    /api/v1/flavor/id/{id}            → findById
GET    /api/v1/flavor/name/{name}        → findByName (X-Next-Cursor header)
GET    /api/v1/flavor/brand/{brandId}    → findByBrandId (X-Next-Cursor header)
```

### Ключевые файлы для чтения перед стартом
- `BrandsPage.tsx` — эталон для FlavorsPage
- `PacksPage.tsx` — файл для модификации
- `src/lib/api.ts` — добавить flavorApi и marketApi
- `src/types/index.ts` — добавить типы
- `FlavorController.kt` — добавить эндпоинты
- `MarketRepository.kt` — расширить findAllViews

---

## File Map

### Backend — создать
- `feature/market/MarketService.kt` — оркестрирует CRUD команды
- `feature/market/api/MarketController.kt` — REST `/api/v1/market`
- `feature/market/api/dto/MarketCreateDto.kt` — DTO для создания
- `feature/market/api/dto/MarketUpdateDto.kt` — DTO для обновления

### Backend — изменить
- `feature/tobacco/flavor/model/FlavorTagEvent.kt` — исправить наследование
- `feature/tobacco/flavor/api/FlavorController.kt` — добавить 2 эндпоинта
- `feature/market/internal/repository/MarketRepository.kt` — расширить `findAllViews`

### Frontend — создать
- `frontend/src/pages/admin/FlavorsPage.tsx` — страница вкусов
- `frontend/src/pages/admin/MarketPage.tsx` — страница market arc

### Frontend — изменить
- `frontend/src/types/index.ts` — добавить Flavor и Market типы
- `frontend/src/lib/api.ts` — добавить `flavorApi`, `marketApi`
- `frontend/src/pages/admin/PacksPage.tsx` — dropdown вкусов в форме
- `frontend/src/App.tsx` — добавить роуты
- `frontend/src/components/Navbar.tsx` — добавить nav items

### Docs
- `CLAUDE.md` — обновить бизнес-контекст

---

## Task 1: Fix FlavorTagEvent bug

**Files:**
- Modify: `src/main/kotlin/com/hookah/kek_hookah/feature/tobacco/flavor/model/FlavorTagEvent.kt`

**Bug:** `FlavorTagCreatedEvent` и `FlavorTagDeleteEvent` расширяют `BrandEvent` вместо `FlavorTagEvent`.

Текущий код:
```kotlin
interface FlavorTagEvent : Event

data class FlavorTagCreatedEvent(
    val flavorTag: FlavorTag,
    override val publishedAt: OffsetDateTime
) : BrandEvent  // ← BUG

data class FlavorTagDeleteEvent(
    val flavorTag: FlavorTag,
    override val publishedAt: OffsetDateTime
) : BrandEvent  // ← BUG
```

- [ ] **Step 1: Исправить FlavorTagEvent.kt**

Заменить оба `BrandEvent` на `FlavorTagEvent`:

```kotlin
package com.hookah.kek_hookah.feature.tobacco.flavor.model

import com.hookah.kek_hookah.infrastructure.event.Event
import java.time.OffsetDateTime

interface FlavorTagEvent : Event

data class FlavorTagCreatedEvent(
    val flavorTag: FlavorTag,
    override val publishedAt: OffsetDateTime
) : FlavorTagEvent

data class FlavorTagDeleteEvent(
    val flavorTag: FlavorTag,
    override val publishedAt: OffsetDateTime
) : FlavorTagEvent
```

- [ ] **Step 2: Убедиться, что компилируется**

```bash
./gradlew compileKotlin
```
Ожидаем: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/hookah/kek_hookah/feature/tobacco/flavor/model/FlavorTagEvent.kt
git commit -m "fix: FlavorTagEvent extends FlavorTagEvent instead of BrandEvent"
```

---

## Task 2: FlavorController — добавить GET /flavor и GET /flavor/search

**Files:**
- Modify: `src/main/kotlin/com/hookah/kek_hookah/feature/tobacco/flavor/api/FlavorController.kt`

FlavorService уже имеет методы `findAll(cursor, limit)` и `findByBrandIdAndNameContaining(brandId, name, cursor, limit)`. Нужно только добавить эндпоинты в контроллер.

- [ ] **Step 1: Добавить два метода в FlavorController.kt**

В конец класса `FlavorController` добавить:

```kotlin
@GetMapping
suspend fun list(
    @RequestParam(required = false) cursor: UUID?,
    @RequestParam(defaultValue = "20") limit: Int
): ResponseEntity<List<TabacoFlavor>> {
    val limited = limit.coerceIn(1, 100)
    val flavors = service.findAll(cursor?.let { FlavorId(it) }, limited)
    val nextCursor = flavors.lastOrNull()?.id?.id?.toString() ?: ""
    return ResponseEntity.ok()
        .header("X-Next-Cursor", nextCursor)
        .body(flavors)
}

@GetMapping("/search")
suspend fun search(
    @RequestParam(required = false) brandId: UUID?,
    @RequestParam(required = false) name: String?,
    @RequestParam(required = false) cursor: UUID?,
    @RequestParam(defaultValue = "20") limit: Int
): ResponseEntity<List<TabacoFlavor>> {
    val limited = limit.coerceIn(1, 100)
    val cur = cursor?.let { FlavorId(it) }
    val flavors = when {
        brandId != null && !name.isNullOrBlank() ->
            service.findByBrandIdAndNameContaining(BrandId(brandId), name, cur, limited)
        brandId != null ->
            service.findByBrandId(BrandId(brandId), cur, limited)
        !name.isNullOrBlank() ->
            service.findAllByName(name, cur, limited)
        else ->
            service.findAll(cur, limited)
    }
    val nextCursor = flavors.lastOrNull()?.id?.id?.toString() ?: ""
    return ResponseEntity.ok()
        .header("X-Next-Cursor", nextCursor)
        .body(flavors)
}
```

- [ ] **Step 2: Проверить компиляцию**

```bash
./gradlew compileKotlin
```
Ожидаем: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/hookah/kek_hookah/feature/tobacco/flavor/api/FlavorController.kt
git commit -m "feat: add GET /flavor and GET /flavor/search endpoints"
```

---

## Task 3: MarketRepository — расширить findAllViews для фильтрации и сортировки

**Files:**
- Modify: `src/main/kotlin/com/hookah/kek_hookah/feature/market/internal/repository/MarketRepository.kt`

Текущий `findAllViews(limit, afterId)` не поддерживает фильтры и сортировку. Нужно расширить его сигнатуру и реализацию.

Допустимые значения `sortBy`: `name`, `brand_name`, `flavor_name`, `weight_grams`, `updated_at` (whitelist для защиты от SQL injection).

- [ ] **Step 1: Заменить метод findAllViews в MarketRepository.kt**

Найти метод `findAllViews` и заменить его:

```kotlin
suspend fun findAllViews(
    limit: Int,
    afterId: UUID? = null,
    brandName: String? = null,
    flavorName: String? = null,
    nameContains: String? = null,
    weightMin: Int? = null,
    weightMax: Int? = null,
    sortBy: String = "updated_at",
    sortDir: String = "desc",
): List<MarketArcView> {
    val allowedSortColumns = setOf("name", "brand_name", "flavor_name", "weight_grams", "updated_at")
    val col = if (sortBy in allowedSortColumns) sortBy else "updated_at"
    val dir = if (sortDir.lowercase() == "asc") "ASC" else "DESC"

    val conditions = mutableListOf<String>()
    if (afterId != null)          conditions += "m.id > :afterId"
    if (!brandName.isNullOrBlank())  conditions += "LOWER(b.name) LIKE :brandName"
    if (!flavorName.isNullOrBlank()) conditions += "LOWER(f.name) LIKE :flavorName"
    if (!nameContains.isNullOrBlank()) conditions += "LOWER(m.name) LIKE :nameContains"
    if (weightMin != null)        conditions += "m.weight_grams >= :weightMin"
    if (weightMax != null)        conditions += "m.weight_grams <= :weightMax"

    val where = if (conditions.isNotEmpty()) " WHERE " + conditions.joinToString(" AND ") else ""
    val sql = VIEW_QUERY + where + " ORDER BY $col $dir, m.id $dir LIMIT :limit"

    var spec = db.sql(sql).bind("limit", limit)
    if (afterId != null)             spec = spec.bind("afterId", afterId)
    if (!brandName.isNullOrBlank())  spec = spec.bind("brandName", "%${brandName.lowercase()}%")
    if (!flavorName.isNullOrBlank()) spec = spec.bind("flavorName", "%${flavorName.lowercase()}%")
    if (!nameContains.isNullOrBlank()) spec = spec.bind("nameContains", "%${nameContains.lowercase()}%")
    if (weightMin != null)           spec = spec.bind("weightMin", weightMin)
    if (weightMax != null)           spec = spec.bind("weightMax", weightMax)

    return spec
        .map { row, _ -> row.toView() }
        .all()
        .collectList()
        .awaitSingle()
}
```

- [ ] **Step 2: Компиляция**

```bash
./gradlew compileKotlin
```
Ожидаем: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/com/hookah/kek_hookah/feature/market/internal/repository/MarketRepository.kt
git commit -m "feat: extend MarketRepository.findAllViews with filters and sorting"
```

---

## Task 4: MarketService

**Files:**
- Create: `src/main/kotlin/com/hookah/kek_hookah/feature/market/MarketService.kt`

- [ ] **Step 1: Создать MarketService.kt**

```kotlin
package com.hookah.kek_hookah.feature.market

import com.hookah.kek_hookah.feature.market.internal.repository.MarketRepository
import com.hookah.kek_hookah.feature.market.internal.usecase.CreateMarketCommand
import com.hookah.kek_hookah.feature.market.internal.usecase.DeleteMarketCommand
import com.hookah.kek_hookah.feature.market.internal.usecase.FindMarketByIdQuery
import com.hookah.kek_hookah.feature.market.internal.usecase.UpdateMarketCommand
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketForCreate
import com.hookah.kek_hookah.feature.market.model.MarketForUpdate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MarketService(
    private val repository: MarketRepository,
    private val createMarketCommand: CreateMarketCommand,
    private val updateMarketCommand: UpdateMarketCommand,
    private val deleteMarketCommand: DeleteMarketCommand,
    private val findMarketByIdQuery: FindMarketByIdQuery,
) {
    suspend fun create(request: MarketForCreate): MarketArcView =
        createMarketCommand.execute(request)

    suspend fun update(request: MarketForUpdate): MarketArcView =
        updateMarketCommand.execute(request)

    suspend fun delete(id: MarketArcId) =
        deleteMarketCommand.execute(id)

    suspend fun findById(id: MarketArcId): MarketArcView? =
        findMarketByIdQuery.execute(id)

    suspend fun list(
        limit: Int,
        afterId: UUID? = null,
        brandName: String? = null,
        flavorName: String? = null,
        nameContains: String? = null,
        weightMin: Int? = null,
        weightMax: Int? = null,
        sortBy: String = "updated_at",
        sortDir: String = "desc",
    ): List<MarketArcView> = repository.findAllViews(
        limit = limit,
        afterId = afterId,
        brandName = brandName,
        flavorName = flavorName,
        nameContains = nameContains,
        weightMin = weightMin,
        weightMax = weightMax,
        sortBy = sortBy,
        sortDir = sortDir,
    )
}
```

- [ ] **Step 2: git add + компиляция**

```bash
git add src/main/kotlin/com/hookah/kek_hookah/feature/market/MarketService.kt
./gradlew compileKotlin
```
Ожидаем: BUILD SUCCESSFUL

---

## Task 5: MarketController + DTOs

**Files:**
- Create: `src/main/kotlin/com/hookah/kek_hookah/feature/market/api/dto/MarketCreateDto.kt`
- Create: `src/main/kotlin/com/hookah/kek_hookah/feature/market/api/dto/MarketUpdateDto.kt`
- Create: `src/main/kotlin/com/hookah/kek_hookah/feature/market/api/MarketController.kt`

- [ ] **Step 1: Создать MarketCreateDto.kt**

```kotlin
package com.hookah.kek_hookah.feature.market.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class MarketCreateDto(
    @NotNull val brandId: UUID,
    @NotNull val flavorId: UUID,
    @NotBlank val name: String,
    @NotNull @Min(1) val weightGrams: Int,
    val gtin: String? = null,
)
```

- [ ] **Step 2: Создать MarketUpdateDto.kt**

```kotlin
package com.hookah.kek_hookah.feature.market.api.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class MarketUpdateDto(
    @NotNull val brandId: UUID,
    @NotNull val flavorId: UUID,
    @NotBlank val name: String,
    @NotNull @Min(1) val weightGrams: Int,
    val gtin: String? = null,
)
```

- [ ] **Step 3: Создать MarketController.kt**

```kotlin
package com.hookah.kek_hookah.feature.market.api

import com.hookah.kek_hookah.feature.auth.model.UserPrincipal
import com.hookah.kek_hookah.feature.market.MarketService
import com.hookah.kek_hookah.feature.market.api.dto.MarketCreateDto
import com.hookah.kek_hookah.feature.market.api.dto.MarketUpdateDto
import com.hookah.kek_hookah.feature.market.model.MarketArcId
import com.hookah.kek_hookah.feature.market.model.MarketArcView
import com.hookah.kek_hookah.feature.market.model.MarketForCreate
import com.hookah.kek_hookah.feature.market.model.MarketForUpdate
import com.hookah.kek_hookah.feature.tobacco.brand.model.BrandId
import com.hookah.kek_hookah.feature.tobacco.flavor.model.FlavorId
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/market")
class MarketController(
    private val service: MarketService
) {

    @PostMapping
    suspend fun create(
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated body: MarketCreateDto
    ): ResponseEntity<MarketArcView> {
        val result = service.create(
            MarketForCreate(
                brandId = BrandId(body.brandId),
                flavorId = FlavorId(body.flavorId),
                name = body.name,
                weightGrams = body.weightGrams,
                gtin = body.gtin,
                updatedBy = user.id,
            )
        )
        return ResponseEntity.ok(result)
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: UUID,
        @AuthenticationPrincipal user: UserPrincipal,
        @RequestBody @Validated body: MarketUpdateDto
    ): ResponseEntity<MarketArcView> {
        val result = service.update(
            MarketForUpdate(
                id = MarketArcId(id),
                brandId = BrandId(body.brandId),
                flavorId = FlavorId(body.flavorId),
                name = body.name,
                weightGrams = body.weightGrams,
                gtin = body.gtin,
                updatedBy = user.id,
            )
        )
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/{id}")
    suspend fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        service.delete(MarketArcId(id))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}")
    suspend fun findById(@PathVariable id: UUID): ResponseEntity<MarketArcView> =
        service.findById(MarketArcId(id))
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping
    suspend fun list(
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) after: UUID?,
        @RequestParam(required = false) brandName: String?,
        @RequestParam(required = false) flavorName: String?,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) weightMin: Int?,
        @RequestParam(required = false) weightMax: Int?,
        @RequestParam(defaultValue = "updated_at") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String,
    ): ResponseEntity<List<MarketArcView>> {
        val limited = limit.coerceIn(1, 100)
        val items = service.list(
            limit = limited,
            afterId = after,
            brandName = brandName,
            flavorName = flavorName,
            nameContains = name,
            weightMin = weightMin,
            weightMax = weightMax,
            sortBy = sortBy,
            sortDir = sortDir,
        )
        val nextCursor = items.lastOrNull()?.id?.id?.toString() ?: ""
        return ResponseEntity.ok()
            .header("X-Next-Cursor", nextCursor)
            .body(items)
    }
}
```

- [ ] **Step 4: Компиляция**

```bash
./gradlew compileKotlin
```
Ожидаем: BUILD SUCCESSFUL

- [ ] **Step 5: git add + commit**

```bash
git add src/main/kotlin/com/hookah/kek_hookah/feature/market/MarketService.kt
git add src/main/kotlin/com/hookah/kek_hookah/feature/market/api/
git commit -m "feat: add MarketService and MarketController with CRUD + filtered list"
```

---

## Task 6: Обновить CLAUDE.md — бизнес-контекст market_arc

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Добавить бизнес-описание market_arc в доменную таблицу**

В разделе `### Domain overview` строку `market_arc` обновить до:

```markdown
| **market_arc** | `market_arc` | Каталог рыночных SKU: конкретный продукт (бренд + вкус + граммовка + GTIN), доступный к закупке. Помогает оператору знать что именно искать при пополнении запасов. |
```

- [ ] **Step 2: Обновить раздел market в Current features**

Найти строку про market и заменить:
```markdown
- **market** — Каталог рыночных позиций (SKU): бренд + вкус + название + вес + GTIN (штрих-код). Используется для поиска продукта при закупке. `/api/v1/market`
```

- [ ] **Step 3: Обновить Route map — добавить маршруты**

```markdown
- `/admin/flavors` → `FlavorsPage` — CRUD вкусов с выбором бренда и управлением тегами
- `/admin/market`  → `MarketPage` — каталог рыночных SKU с фильтрацией, сортировкой, CRUD
```

- [ ] **Step 4: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: update CLAUDE.md with market_arc business context and new routes"
```

---

## Task 7: Frontend — обновить types/index.ts

**Files:**
- Modify: `frontend/src/types/index.ts`

- [ ] **Step 1: Добавить Flavor типы в конец секции Brands (перед Packs)**

```typescript
// Flavors
export interface TabacoFlavor {
  id: string
  brandId: string
  name: string
  description?: string
  strength?: number
  tags: Tag[]
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface FlavorCreateRequest {
  brandId: string
  name: string
  description?: string
  strength: number
}

export interface FlavorUpdateRequest {
  brandId: string
  name: string
  description?: string
  strength: number
}

export interface UpdateTagForFlavorRequest {
  flavorId: string
  tagId: string
}
```

- [ ] **Step 2: Добавить Market типы в конец файла**

```typescript
// Market Arc
export interface MarketArcView {
  id: string
  brandId: string
  brandName: string
  flavorId: string
  flavorName: string
  name: string
  weightGrams: number
  gtin?: string
  createdAt: string
  updatedAt: string
  updatedBy: string
}

export interface MarketCreateRequest {
  brandId: string
  flavorId: string
  name: string
  weightGrams: number
  gtin?: string
}

export interface MarketUpdateRequest {
  brandId: string
  flavorId: string
  name: string
  weightGrams: number
  gtin?: string
}

export interface MarketListParams {
  limit?: number
  after?: string
  brandName?: string
  flavorName?: string
  name?: string
  weightMin?: number
  weightMax?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
}
```

- [ ] **Step 3: Commit**

```bash
git add frontend/src/types/index.ts
git commit -m "feat: add Flavor and MarketArc TypeScript types"
```

---

## Task 8: Frontend — обновить api.ts (flavorApi + marketApi)

**Files:**
- Modify: `frontend/src/lib/api.ts`

Паттерн: axios instance называется `api`, базовый URL `/api/v1`. Cursor-based пагинация: cursor читается из response header `X-Next-Cursor`.

- [ ] **Step 1: Добавить flavorApi после brandApi**

```typescript
export const flavorApi = {
  list: (params: { cursor?: string; limit?: number } = {}) =>
    api.get<TabacoFlavor[]>('/flavor', { params: { cursor: params.cursor, limit: params.limit ?? 20 } }),

  search: (params: { brandId?: string; name?: string; cursor?: string; limit?: number }) =>
    api.get<TabacoFlavor[]>('/flavor/search', { params: { ...params, limit: params.limit ?? 20 } }),

  findByBrandId: (brandId: string, params: { cursor?: string; limit?: number } = {}) =>
    api.get<TabacoFlavor[]>(`/flavor/brand/${brandId}`, { params: { cursor: params.cursor, limit: params.limit ?? 20 } }),

  findById: (id: string) =>
    api.get<TabacoFlavor>(`/flavor/id/${id}`),

  create: (body: FlavorCreateRequest) =>
    api.post<TabacoFlavor>('/flavor', body),

  update: (id: string, body: FlavorUpdateRequest) =>
    api.put<TabacoFlavor>(`/flavor/${id}`, body),

  addTag: (body: UpdateTagForFlavorRequest) =>
    api.patch<TabacoFlavor>('/flavor/add-tag', body),

  removeTag: (body: UpdateTagForFlavorRequest) =>
    api.patch<TabacoFlavor>('/flavor/remove-tag', body),
}
```

- [ ] **Step 2: Добавить marketApi**

```typescript
export const marketApi = {
  list: (params: MarketListParams = {}) =>
    api.get<MarketArcView[]>('/market', { params }),

  findById: (id: string) =>
    api.get<MarketArcView>(`/market/${id}`),

  create: (body: MarketCreateRequest) =>
    api.post<MarketArcView>('/market', body),

  update: (id: string, body: MarketUpdateRequest) =>
    api.put<MarketArcView>(`/market/${id}`, body),

  delete: (id: string) =>
    api.delete(`/market/${id}`),
}
```

- [ ] **Step 3: Добавить импорты типов** — убедиться, что в начале файла `api.ts` все новые типы импортированы из `@/types`.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/lib/api.ts
git commit -m "feat: add flavorApi and marketApi to api.ts"
```

---

## Task 9: FlavorsPage.tsx

**Files:**
- Create: `frontend/src/pages/admin/FlavorsPage.tsx`

**Логика страницы:**
1. Сверху — поиск/выбор бренда: text input с debounce, вызывает `brandApi.findByName(q)` при >=2 символах или `brandApi.list()` для показа dropdown. Выбранный бренд фиксируется.
2. После выбора бренда — поисковая строка по имени вкуса.
3. Список вкусов = `useInfiniteQuery` на `GET /flavor/search?brandId=...&name=...`, cursor из header `X-Next-Cursor`.
4. Карточка вкуса: имя, описание, strength (бейджик 0–10), теги, кнопка редактировать.
5. FlavorFormDialog — модал создания/редактирования: бренд залочен (показывается как текст), поля: имя, описание, strength (slider или number input 0–10).
6. FlavorTagsDialog — аналог BrandTagsDialog: теги вкуса с × и dropdown для добавления.

**Паттерн cursor для useInfiniteQuery:**
```typescript
useInfiniteQuery({
  queryKey: ['flavors', selectedBrandId, searchName],
  queryFn: async ({ pageParam }) => {
    const res = await flavorApi.search({
      brandId: selectedBrandId,
      name: searchName || undefined,
      cursor: pageParam || undefined,
    })
    return { data: res.data, nextCursor: res.headers['x-next-cursor'] || '' }
  },
  getNextPageParam: (last) => last.nextCursor || undefined,
  initialPageParam: '',
  enabled: !!selectedBrandId,
})
```

- [ ] **Step 1: Создать FlavorsPage.tsx** — полная реализация по образцу `BrandsPage.tsx`. Ключевые отличия от BrandsPage:
  - Добавить `BrandSelector` компонент сверху (input + dropdown брендов)
  - Список вкусов зависит от выбранного бренда (`enabled: !!selectedBrandId`)
  - Карточка показывает strength как цветной бейджик: 0–3 зелёный, 4–6 жёлтый, 7–10 красный
  - Форма имеет поле strength (input type="number" min=0 max=10)
  - `TagsDialog` использует `flavorApi.addTag` / `flavorApi.removeTag` с полем `flavorId`

```tsx
import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Search, X, ChevronDown, Flame, Tag as TagIcon } from 'lucide-react'
import { flavorApi, brandApi } from '@/lib/api'
import type { TabacoFlavor, TabacoBrand, Tag, FlavorCreateRequest, FlavorUpdateRequest } from '@/types'

// ── BrandSelector ──────────────────────────────────────────────────────────
function BrandSelector({
  selected, onSelect,
}: { selected: TabacoBrand | null; onSelect: (b: TabacoBrand) => void }) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  const { data: brands } = useInfiniteQuery({
    queryKey: ['brands-selector', query],
    queryFn: async ({ pageParam }) => {
      if (query.length >= 2) {
        const res = await brandApi.findByName(query)
        return { data: Array.isArray(res.data) ? res.data : [res.data], nextCursor: '' }
      }
      const res = await brandApi.list({ limit: 20, after: pageParam || undefined })
      return { data: res.data.items, nextCursor: res.data.nextToken || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })

  const items = brands?.pages.flatMap(p => p.data) ?? []

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  return (
    <div ref={ref} className="relative w-full max-w-sm">
      <div
        className="flex items-center gap-2 px-3 py-2 rounded-lg border border-border bg-surface cursor-pointer hover:border-gold transition-colors"
        onClick={() => setOpen(o => !o)}
      >
        <Search className="h-4 w-4 text-ink-muted flex-shrink-0" />
        {selected ? (
          <span className="text-sm font-body text-ink flex-1">{selected.name}</span>
        ) : (
          <input
            className="bg-transparent text-sm font-body text-ink placeholder-ink-muted outline-none flex-1"
            placeholder="Выберите бренд..."
            value={query}
            onChange={e => { setQuery(e.target.value); setOpen(true) }}
            onClick={e => { e.stopPropagation(); setOpen(true) }}
          />
        )}
        {selected ? (
          <button onClick={e => { e.stopPropagation(); onSelect(null as any); setQuery('') }}>
            <X className="h-3.5 w-3.5 text-ink-muted hover:text-crimson" />
          </button>
        ) : (
          <ChevronDown className="h-3.5 w-3.5 text-ink-muted" />
        )}
      </div>
      {open && items.length > 0 && (
        <div className="absolute z-50 top-full mt-1 w-full bg-elevated border border-border rounded-lg shadow-lg max-h-52 overflow-y-auto">
          {items.map(b => (
            <button
              key={b.id}
              className="w-full text-left px-3 py-2 text-sm font-body text-ink hover:bg-hover transition-colors"
              onClick={() => { onSelect(b); setOpen(false); setQuery('') }}
            >
              {b.name}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

// ── StrengthBadge ──────────────────────────────────────────────────────────
function StrengthBadge({ value }: { value?: number }) {
  if (value == null) return null
  const color = value <= 3 ? 'text-green-400 bg-green-900/20 border-green-800'
    : value <= 6 ? 'text-yellow-400 bg-yellow-900/20 border-yellow-800'
    : 'text-crimson bg-crimson/10 border-crimson/30'
  return (
    <span className={`inline-flex items-center px-1.5 py-0.5 rounded border text-xs font-body font-medium ${color}`}>
      {value}/10
    </span>
  )
}

// ── TagDropdown ────────────────────────────────────────────────────────────
function TagDropdown({ allTags, assigned, onAdd }: {
  allTags: Tag[]; assigned: Tag[]; onAdd: (tag: Tag) => void
}) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const ref = useRef<HTMLDivElement>(null)
  const available = allTags.filter(t => !assigned.find(a => a.id === t.id) && t.name.toLowerCase().includes(query.toLowerCase()))

  useEffect(() => {
    const h = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen(o => !o)}
        className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border border-dashed border-border text-xs text-ink-dim hover:border-gold hover:text-gold transition-colors"
      >
        <Plus className="h-3 w-3" /> Добавить тег
      </button>
      {open && (
        <div className="absolute z-50 top-full mt-1 w-56 bg-elevated border border-border rounded-lg shadow-lg">
          <div className="p-2 border-b border-border">
            <input
              autoFocus
              className="w-full bg-transparent text-xs font-body text-ink placeholder-ink-muted outline-none"
              placeholder="Поиск тега..."
              value={query}
              onChange={e => setQuery(e.target.value)}
            />
          </div>
          <div className="max-h-40 overflow-y-auto">
            {available.length === 0 ? (
              <p className="px-3 py-2 text-xs text-ink-muted">Теги не найдены</p>
            ) : available.map(t => (
              <button
                key={t.id}
                className="w-full text-left px-3 py-1.5 text-xs font-body text-ink hover:bg-hover transition-colors"
                onClick={() => { onAdd(t); setOpen(false); setQuery('') }}
              >
                {t.name}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

// ── FlavorTagsDialog ────────────────────────────────────────────────────────
function FlavorTagsDialog({ flavor, allTags, onClose }: {
  flavor: TabacoFlavor; allTags: Tag[]; onClose: () => void
}) {
  const qc = useQueryClient()
  const [tags, setTags] = useState<Tag[]>(flavor.tags)

  const addMut = useMutation({
    mutationFn: (tag: Tag) => flavorApi.addTag({ flavorId: flavor.id, tagId: tag.id }),
    onSuccess: (_, tag) => {
      setTags(prev => [...prev, tag])
      qc.invalidateQueries({ queryKey: ['flavors'] })
    },
    onError: () => toast.error('Не удалось добавить тег'),
  })

  const removeMut = useMutation({
    mutationFn: (tag: Tag) => flavorApi.removeTag({ flavorId: flavor.id, tagId: tag.id }),
    onSuccess: (_, tag) => {
      setTags(prev => prev.filter(t => t.id !== tag.id))
      qc.invalidateQueries({ queryKey: ['flavors'] })
    },
    onError: () => toast.error('Не удалось удалить тег'),
  })

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
      <div className="bg-surface border border-border rounded-xl p-6 w-full max-w-md shadow-2xl" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-display text-base text-ink">Теги: {flavor.name}</h2>
          <button onClick={onClose}><X className="h-4 w-4 text-ink-dim hover:text-crimson" /></button>
        </div>
        <div className="flex flex-wrap gap-2 mb-4 min-h-[2rem]">
          {tags.map(t => (
            <span key={t.id} className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-gold/10 border border-gold/30 text-xs text-gold">
              {t.name}
              <button onClick={() => removeMut.mutate(t)} className="hover:text-crimson transition-colors">
                <X className="h-2.5 w-2.5" />
              </button>
            </span>
          ))}
          {tags.length === 0 && <p className="text-xs text-ink-muted">Нет тегов</p>}
        </div>
        <TagDropdown allTags={allTags} assigned={tags} onAdd={t => addMut.mutate(t)} />
      </div>
    </div>
  )
}

// ── FlavorFormDialog ────────────────────────────────────────────────────────
function FlavorFormDialog({ flavor, brand, onClose }: {
  flavor: TabacoFlavor | null; brand: TabacoBrand; onClose: () => void
}) {
  const qc = useQueryClient()
  const [form, setForm] = useState({
    name: flavor?.name ?? '',
    description: flavor?.description ?? '',
    strength: flavor?.strength ?? 5,
  })

  const mut = useMutation({
    mutationFn: () => {
      const body: FlavorCreateRequest | FlavorUpdateRequest = {
        brandId: brand.id,
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        strength: form.strength,
      }
      return flavor ? flavorApi.update(flavor.id, body) : flavorApi.create(body)
    },
    onSuccess: () => {
      toast.success(flavor ? 'Вкус обновлён' : 'Вкус создан')
      qc.invalidateQueries({ queryKey: ['flavors'] })
      onClose()
    },
    onError: () => toast.error('Ошибка при сохранении'),
  })

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
      <div className="bg-surface border border-border rounded-xl p-6 w-full max-w-md shadow-2xl" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-display text-base text-ink">{flavor ? 'Редактировать вкус' : 'Новый вкус'}</h2>
          <button onClick={onClose}><X className="h-4 w-4 text-ink-dim hover:text-crimson" /></button>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Бренд</label>
            <p className="text-sm font-body text-ink-dim px-3 py-2 rounded-lg bg-deep border border-border">{brand.name}</p>
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Название *</label>
            <input
              className="w-full px-3 py-2 rounded-lg border border-border bg-deep text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
              value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              placeholder="Название вкуса"
            />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Описание</label>
            <textarea
              rows={2}
              className="w-full px-3 py-2 rounded-lg border border-border bg-deep text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors resize-none"
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              placeholder="Описание вкуса"
            />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Крепость: {form.strength}/10</label>
            <input
              type="range" min={0} max={10} step={1}
              className="w-full accent-gold"
              value={form.strength}
              onChange={e => setForm(f => ({ ...f, strength: Number(e.target.value) }))}
            />
            <div className="flex justify-between text-xs text-ink-muted mt-1">
              <span>Лёгкий</span><span>Крепкий</span>
            </div>
          </div>
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <button onClick={onClose} className="px-4 py-2 rounded-lg text-sm font-body text-ink-dim border border-border hover:bg-hover transition-colors">
            Отмена
          </button>
          <button
            onClick={() => mut.mutate()}
            disabled={!form.name.trim() || mut.isPending}
            className="px-4 py-2 rounded-lg text-sm font-body font-medium bg-gold/10 border border-gold/30 text-gold hover:bg-gold/20 disabled:opacity-50 transition-colors"
          >
            {mut.isPending ? 'Сохранение...' : (flavor ? 'Сохранить' : 'Создать')}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── FlavorCard ──────────────────────────────────────────────────────────────
function FlavorCard({ flavor, allTags, onEdit }: {
  flavor: TabacoFlavor; allTags: Tag[]; onEdit: () => void
}) {
  const [showTags, setShowTags] = useState(false)
  return (
    <div className="group relative bg-surface border border-border rounded-xl p-4 hover:border-gold/40 transition-all">
      <div className="flex items-start justify-between gap-2 mb-2">
        <div className="flex items-center gap-2">
          <h3 className="font-display text-sm text-ink leading-tight">{flavor.name}</h3>
          <StrengthBadge value={flavor.strength} />
        </div>
        <button
          onClick={onEdit}
          className="opacity-0 group-hover:opacity-100 text-xs font-body text-ink-muted hover:text-gold transition-all px-2 py-1 rounded border border-transparent hover:border-border"
        >
          Изм.
        </button>
      </div>
      {flavor.description && (
        <p className="text-xs font-body text-ink-dim mb-3 line-clamp-2">{flavor.description}</p>
      )}
      <div className="flex flex-wrap gap-1.5 mb-2">
        {flavor.tags.map(t => (
          <span key={t.id} className="px-2 py-0.5 rounded-full bg-gold/10 border border-gold/20 text-xs text-gold">{t.name}</span>
        ))}
      </div>
      <button
        onClick={() => setShowTags(true)}
        className="flex items-center gap-1 text-xs font-body text-ink-muted hover:text-gold transition-colors"
      >
        <TagIcon className="h-3 w-3" /> Теги
      </button>
      {showTags && <FlavorTagsDialog flavor={flavor} allTags={allTags} onClose={() => setShowTags(false)} />}
    </div>
  )
}

// ── FlavorsPage ──────────────────────────────────────────────────────────────
export default function FlavorsPage() {
  const [selectedBrand, setSelectedBrand] = useState<TabacoBrand | null>(null)
  const [search, setSearch] = useState('')
  const [editFlavor, setEditFlavor] = useState<TabacoFlavor | null | 'new'>('new')
  const [showForm, setShowForm] = useState(false)

  // All tags (для TagDropdown) — берём из брендов как в BrandsPage
  const { data: brandsData } = useInfiniteQuery({
    queryKey: ['brands-for-tags'],
    queryFn: async ({ pageParam }) => {
      const res = await brandApi.list({ limit: 100, after: pageParam || undefined })
      return { data: res.data.items, nextCursor: res.data.nextToken || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })
  const allTags: Tag[] = Array.from(
    new Map(
      (brandsData?.pages.flatMap(p => p.data.flatMap(b => b.tags)) ?? []).map(t => [t.id, t])
    ).values()
  )

  const query = useInfiniteQuery({
    queryKey: ['flavors', selectedBrand?.id, search],
    queryFn: async ({ pageParam }) => {
      const res = await flavorApi.search({
        brandId: selectedBrand?.id,
        name: search || undefined,
        cursor: pageParam || undefined,
        limit: 20,
      })
      return { data: res.data, nextCursor: res.headers['x-next-cursor'] || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
    enabled: !!selectedBrand,
  })

  const flavors = query.data?.pages.flatMap(p => p.data) ?? []

  return (
    <div className="page-root">
      <div className="page-container page-enter">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-gold/10 border border-gold/30 flex items-center justify-center">
              <Flame className="h-4 w-4 text-gold" />
            </div>
            <div>
              <h1 className="font-display text-xl text-ink">Вкусы</h1>
              <p className="text-xs font-body text-ink-muted">Управление вкусами табака</p>
            </div>
          </div>
          {selectedBrand && (
            <button
              onClick={() => { setEditFlavor(null); setShowForm(true) }}
              className="flex items-center gap-2 px-3 py-2 rounded-lg bg-gold/10 border border-gold/30 text-gold text-sm font-body hover:bg-gold/20 transition-colors"
            >
              <Plus className="h-4 w-4" /> Добавить
            </button>
          )}
        </div>

        {/* Brand Selector + Search */}
        <div className="flex flex-col sm:flex-row gap-3 mb-6">
          <BrandSelector selected={selectedBrand} onSelect={b => { setSelectedBrand(b); setSearch('') }} />
          {selectedBrand && (
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted" />
              <input
                className="w-full pl-9 pr-3 py-2 rounded-lg border border-border bg-surface text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
                placeholder="Поиск по названию..."
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>
          )}
        </div>

        {/* Empty state */}
        {!selectedBrand && (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <Flame className="h-12 w-12 text-ink-muted mb-4" />
            <p className="font-display text-lg text-ink-dim">Выберите бренд</p>
            <p className="text-sm font-body text-ink-muted mt-1">чтобы увидеть вкусы</p>
          </div>
        )}

        {/* Grid */}
        {selectedBrand && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {query.isLoading
              ? Array.from({ length: 6 }).map((_, i) => (
                  <div key={i} className="h-32 rounded-xl bg-surface border border-border animate-pulse" />
                ))
              : flavors.map(f => (
                  <FlavorCard
                    key={f.id}
                    flavor={f}
                    allTags={allTags}
                    onEdit={() => { setEditFlavor(f); setShowForm(true) }}
                  />
                ))
            }
          </div>
        )}

        {/* Load more */}
        {query.hasNextPage && (
          <div className="flex justify-center mt-6">
            <button
              onClick={() => query.fetchNextPage()}
              disabled={query.isFetchingNextPage}
              className="px-6 py-2 rounded-lg border border-border text-sm font-body text-ink-dim hover:bg-hover transition-colors disabled:opacity-50"
            >
              {query.isFetchingNextPage ? 'Загрузка...' : 'Ещё'}
            </button>
          </div>
        )}

        {/* Form dialog */}
        {showForm && selectedBrand && (
          <FlavorFormDialog
            flavor={editFlavor === 'new' || editFlavor === null ? null : editFlavor}
            brand={selectedBrand}
            onClose={() => setShowForm(false)}
          />
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: git add + убедиться, что нет TypeScript ошибок**

```bash
git add frontend/src/pages/admin/FlavorsPage.tsx
cd frontend && npm run lint
```
Ожидаем: выход без критических ошибок типов. Исправить если есть.

- [ ] **Step 3: Commit**

```bash
git commit -m "feat: add FlavorsPage with brand selector, tags, CRUD"
```

---

## Task 10: PacksPage — добавить flavor dropdown в форму

**Files:**
- Modify: `frontend/src/pages/admin/PacksPage.tsx`

Текущая форма показывает `flavorId` как text input. Нужно заменить на search-as-you-type dropdown вкусов.

- [ ] **Step 1: Добавить импорт flavorApi**

В `PacksPage.tsx` добавить в импорты:
```typescript
import { flavorApi } from '@/lib/api'
import type { TabacoFlavor } from '@/types'
```

- [ ] **Step 2: Добавить FlavorSelector компонент внутри файла** (перед PackFormDialog)

```tsx
function FlavorSelector({ value, onChange }: { value: string; onChange: (id: string, name: string) => void }) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const [selectedName, setSelectedName] = useState('')
  const ref = useRef<HTMLDivElement>(null)

  const { data } = useInfiniteQuery({
    queryKey: ['flavors-selector', query],
    queryFn: async ({ pageParam }) => {
      const res = await flavorApi.search({ name: query || undefined, cursor: pageParam || undefined, limit: 20 })
      return { data: res.data, nextCursor: res.headers['x-next-cursor'] || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })
  const flavors = data?.pages.flatMap(p => p.data) ?? []

  useEffect(() => {
    const h = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  const displayValue = selectedName || (value ? `ID: ${value.slice(0, 8)}...` : '')

  return (
    <div ref={ref} className="relative">
      <div
        className="flex items-center gap-2 w-full px-3 py-2 rounded-lg border border-border bg-deep cursor-pointer hover:border-gold transition-colors"
        onClick={() => setOpen(o => !o)}
      >
        <span className="text-sm font-body text-ink flex-1 truncate">
          {displayValue || <span className="text-ink-muted">Без вкуса</span>}
        </span>
        {value && (
          <button onClick={e => { e.stopPropagation(); onChange('', ''); setSelectedName('') }}>
            <X className="h-3.5 w-3.5 text-ink-muted hover:text-crimson" />
          </button>
        )}
        <ChevronDown className="h-3.5 w-3.5 text-ink-muted flex-shrink-0" />
      </div>
      {open && (
        <div className="absolute z-50 top-full mt-1 w-full bg-elevated border border-border rounded-lg shadow-lg">
          <div className="p-2 border-b border-border">
            <input
              autoFocus
              className="w-full bg-transparent text-xs font-body text-ink placeholder-ink-muted outline-none"
              placeholder="Поиск вкуса..."
              value={query}
              onChange={e => setQuery(e.target.value)}
            />
          </div>
          <div className="max-h-48 overflow-y-auto">
            <button
              className="w-full text-left px-3 py-2 text-sm font-body text-ink-muted hover:bg-hover transition-colors"
              onClick={() => { onChange('', ''); setSelectedName(''); setOpen(false) }}
            >
              Без вкуса
            </button>
            {flavors.map((f: TabacoFlavor) => (
              <button
                key={f.id}
                className="w-full text-left px-3 py-2 text-sm font-body text-ink hover:bg-hover transition-colors"
                onClick={() => { onChange(f.id, f.name); setSelectedName(f.name); setOpen(false) }}
              >
                {f.name}
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
```

- [ ] **Step 3: Заменить поле flavorId в PackFormDialog**

Найти в `PackFormDialog` input для `flavorId` и заменить на:
```tsx
<div>
  <label className="block text-xs font-body text-ink-muted mb-1">Вкус</label>
  <FlavorSelector
    value={form.flavorId}
    onChange={(id, name) => setForm(f => ({ ...f, flavorId: id }))}
  />
</div>
```

- [ ] **Step 4: В PackCard заменить отображение flavorId**

Найти место где показывается `pack.flavorId` (обычно как raw UUID) и добавить загрузку имени вкуса — или просто показать укороченный ID с тултипом. Если в PackCard используется `pack.flavorId`, заменить на:
```tsx
{pack.flavorId && (
  <span className="text-xs font-body text-ink-muted">Вкус: {pack.flavorId.slice(0, 8)}…</span>
)}
```
(Полная загрузка имени вкуса — будущая задача, для MVP достаточно ID.)

- [ ] **Step 5: Добавить нужные импорты** (`ChevronDown`, `flavorApi`, `TabacoFlavor`, `useInfiniteQuery`)

- [ ] **Step 6: TypeScript lint проверка**

```bash
cd frontend && npm run lint
```

- [ ] **Step 7: Commit**

```bash
git add frontend/src/pages/admin/PacksPage.tsx
git commit -m "feat: add flavor search dropdown to PacksPage form"
```

---

## Task 11: MarketPage.tsx

**Files:**
- Create: `frontend/src/pages/admin/MarketPage.tsx`

**Логика:**
- Панель фильтров: поиск по названию SKU, по бренду, по вкусу; фильтр веса (мин/макс)
- Dropdown сортировки: поле + направление
- `useInfiniteQuery` на `marketApi.list(filters)`, cursor из header `X-Next-Cursor`
- Карточка: название SKU + бренд/вкус бейджики, вес, GTIN (если есть), timestamp
- CRUD: `MarketFormDialog` — выбор бренда → выбор вкуса (BrandSelector + FlavorSelector), поля: название, вес, GTIN
- Delete: confirmation dialog (аналог PacksPage `DeletePackDialog`)

- [ ] **Step 1: Создать MarketPage.tsx**

```tsx
import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Search, X, ChevronDown, ShoppingBag, Trash2, ChevronUp } from 'lucide-react'
import { marketApi, brandApi, flavorApi } from '@/lib/api'
import type { MarketArcView, MarketCreateRequest, MarketUpdateRequest, TabacoBrand, TabacoFlavor } from '@/types'

const SORT_OPTIONS = [
  { value: 'updated_at', label: 'Дата обновления' },
  { value: 'name', label: 'Название' },
  { value: 'brand_name', label: 'Бренд' },
  { value: 'flavor_name', label: 'Вкус' },
  { value: 'weight_grams', label: 'Вес' },
]

// ── BrandSelectorCompact ───────────────────────────────────────────────────
// Упрощённый вариант BrandSelector из FlavorsPage (тот же паттерн)
function BrandSelectorCompact({ value, onChange }: {
  value: TabacoBrand | null; onChange: (b: TabacoBrand | null) => void
}) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  const { data } = useInfiniteQuery({
    queryKey: ['brands-mkt', query],
    queryFn: async ({ pageParam }) => {
      if (query.length >= 2) {
        const res = await brandApi.findByName(query)
        return { data: Array.isArray(res.data) ? res.data : [res.data], nextCursor: '' }
      }
      const res = await brandApi.list({ limit: 20, after: pageParam || undefined })
      return { data: res.data.items, nextCursor: res.data.nextToken || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })
  const items = data?.pages.flatMap(p => p.data) ?? []

  useEffect(() => {
    const h = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  return (
    <div ref={ref} className="relative">
      <div
        className="flex items-center gap-2 px-3 py-2 rounded-lg border border-border bg-deep cursor-pointer hover:border-gold transition-colors min-w-[160px]"
        onClick={() => setOpen(o => !o)}
      >
        {value ? (
          <>
            <span className="text-sm font-body text-ink flex-1 truncate">{value.name}</span>
            <button onClick={e => { e.stopPropagation(); onChange(null); setQuery('') }}>
              <X className="h-3.5 w-3.5 text-ink-muted hover:text-crimson" />
            </button>
          </>
        ) : (
          <>
            <input
              className="bg-transparent text-sm font-body text-ink placeholder-ink-muted outline-none flex-1 min-w-0"
              placeholder="Бренд..."
              value={query}
              onChange={e => { setQuery(e.target.value); setOpen(true) }}
              onClick={e => { e.stopPropagation(); setOpen(true) }}
            />
            <ChevronDown className="h-3.5 w-3.5 text-ink-muted flex-shrink-0" />
          </>
        )}
      </div>
      {open && items.length > 0 && (
        <div className="absolute z-50 top-full mt-1 w-full bg-elevated border border-border rounded-lg shadow-lg max-h-48 overflow-y-auto">
          {items.map(b => (
            <button key={b.id} className="w-full text-left px-3 py-2 text-sm font-body text-ink hover:bg-hover transition-colors"
              onClick={() => { onChange(b); setOpen(false); setQuery('') }}>
              {b.name}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

// ── FlavorSelectorCompact ──────────────────────────────────────────────────
function FlavorSelectorCompact({ brandId, value, onChange }: {
  brandId?: string; value: TabacoFlavor | null; onChange: (f: TabacoFlavor | null) => void
}) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  const { data } = useInfiniteQuery({
    queryKey: ['flavors-mkt', brandId, query],
    queryFn: async ({ pageParam }) => {
      const res = await flavorApi.search({ brandId, name: query || undefined, cursor: pageParam || undefined, limit: 20 })
      return { data: res.data, nextCursor: res.headers['x-next-cursor'] || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
    enabled: true,
  })
  const flavors = data?.pages.flatMap(p => p.data) ?? []

  useEffect(() => {
    const h = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  return (
    <div ref={ref} className="relative">
      <div
        className="flex items-center gap-2 px-3 py-2 rounded-lg border border-border bg-deep cursor-pointer hover:border-gold transition-colors min-w-[160px]"
        onClick={() => setOpen(o => !o)}
      >
        {value ? (
          <>
            <span className="text-sm font-body text-ink flex-1 truncate">{value.name}</span>
            <button onClick={e => { e.stopPropagation(); onChange(null); setQuery('') }}>
              <X className="h-3.5 w-3.5 text-ink-muted hover:text-crimson" />
            </button>
          </>
        ) : (
          <>
            <input
              className="bg-transparent text-sm font-body text-ink placeholder-ink-muted outline-none flex-1 min-w-0"
              placeholder="Вкус..."
              value={query}
              onChange={e => { setQuery(e.target.value); setOpen(true) }}
              onClick={e => { e.stopPropagation(); setOpen(true) }}
            />
            <ChevronDown className="h-3.5 w-3.5 text-ink-muted flex-shrink-0" />
          </>
        )}
      </div>
      {open && (
        <div className="absolute z-50 top-full mt-1 w-full bg-elevated border border-border rounded-lg shadow-lg max-h-48 overflow-y-auto">
          <button className="w-full text-left px-3 py-2 text-sm font-body text-ink-muted hover:bg-hover"
            onClick={() => { onChange(null); setOpen(false) }}>Любой вкус</button>
          {flavors.map((f: TabacoFlavor) => (
            <button key={f.id} className="w-full text-left px-3 py-2 text-sm font-body text-ink hover:bg-hover transition-colors"
              onClick={() => { onChange(f); setOpen(false); setQuery('') }}>
              {f.name}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}

// ── MarketFormDialog ────────────────────────────────────────────────────────
function MarketFormDialog({ item, onClose }: { item: MarketArcView | null; onClose: () => void }) {
  const qc = useQueryClient()
  const [brand, setBrand] = useState<TabacoBrand | null>(null)
  const [flavor, setFlavor] = useState<TabacoFlavor | null>(null)
  const [form, setForm] = useState({
    name: item?.name ?? '',
    weightGrams: item?.weightGrams ?? 25,
    gtin: item?.gtin ?? '',
  })

  const mut = useMutation({
    mutationFn: () => {
      if (!brand || !flavor) throw new Error('Выберите бренд и вкус')
      const body: MarketCreateRequest | MarketUpdateRequest = {
        brandId: brand.id,
        flavorId: flavor.id,
        name: form.name.trim(),
        weightGrams: form.weightGrams,
        gtin: form.gtin.trim() || undefined,
      }
      return item ? marketApi.update(item.id, body) : marketApi.create(body)
    },
    onSuccess: () => {
      toast.success(item ? 'SKU обновлён' : 'SKU создан')
      qc.invalidateQueries({ queryKey: ['market'] })
      onClose()
    },
    onError: (e: any) => toast.error(e.message || 'Ошибка при сохранении'),
  })

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
      <div className="bg-surface border border-border rounded-xl p-6 w-full max-w-md shadow-2xl" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-display text-base text-ink">{item ? 'Редактировать SKU' : 'Новый SKU'}</h2>
          <button onClick={onClose}><X className="h-4 w-4 text-ink-dim hover:text-crimson" /></button>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Бренд *</label>
            <BrandSelectorCompact value={brand} onChange={b => { setBrand(b); setFlavor(null) }} />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Вкус *</label>
            <FlavorSelectorCompact brandId={brand?.id} value={flavor} onChange={setFlavor} />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Название SKU *</label>
            <input
              className="w-full px-3 py-2 rounded-lg border border-border bg-deep text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
              value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              placeholder="Например: Darkside Generis 100g"
            />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">Вес (г) *</label>
            <input
              type="number" min={1}
              className="w-full px-3 py-2 rounded-lg border border-border bg-deep text-sm font-body text-ink outline-none focus:border-gold transition-colors"
              value={form.weightGrams}
              onChange={e => setForm(f => ({ ...f, weightGrams: Number(e.target.value) }))}
            />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1">GTIN / Штрих-код</label>
            <input
              className="w-full px-3 py-2 rounded-lg border border-border bg-deep text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
              value={form.gtin}
              onChange={e => setForm(f => ({ ...f, gtin: e.target.value }))}
              placeholder="Необязательно"
            />
          </div>
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <button onClick={onClose} className="px-4 py-2 rounded-lg text-sm font-body text-ink-dim border border-border hover:bg-hover transition-colors">
            Отмена
          </button>
          <button
            onClick={() => mut.mutate()}
            disabled={!brand || !flavor || !form.name.trim() || form.weightGrams <= 0 || mut.isPending}
            className="px-4 py-2 rounded-lg text-sm font-body font-medium bg-gold/10 border border-gold/30 text-gold hover:bg-gold/20 disabled:opacity-50 transition-colors"
          >
            {mut.isPending ? 'Сохранение...' : (item ? 'Сохранить' : 'Создать')}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── DeleteMarketDialog ──────────────────────────────────────────────────────
function DeleteMarketDialog({ item, onClose }: { item: MarketArcView; onClose: () => void }) {
  const qc = useQueryClient()
  const mut = useMutation({
    mutationFn: () => marketApi.delete(item.id),
    onSuccess: () => {
      toast.success('SKU удалён')
      qc.invalidateQueries({ queryKey: ['market'] })
      onClose()
    },
    onError: () => toast.error('Не удалось удалить'),
  })
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
      <div className="bg-surface border border-border rounded-xl p-6 w-full max-w-sm shadow-2xl" onClick={e => e.stopPropagation()}>
        <h2 className="font-display text-base text-ink mb-2">Удалить SKU?</h2>
        <p className="text-sm font-body text-ink-dim mb-5">«{item.name}» будет удалён без возможности восстановления.</p>
        <div className="flex justify-end gap-3">
          <button onClick={onClose} className="px-4 py-2 rounded-lg text-sm font-body text-ink-dim border border-border hover:bg-hover transition-colors">
            Отмена
          </button>
          <button
            onClick={() => mut.mutate()}
            disabled={mut.isPending}
            className="px-4 py-2 rounded-lg text-sm font-body font-medium bg-crimson/10 border border-crimson/30 text-crimson hover:bg-crimson/20 disabled:opacity-50 transition-colors"
          >
            {mut.isPending ? 'Удаление...' : 'Удалить'}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── MarketCard ──────────────────────────────────────────────────────────────
function MarketCard({ item, onEdit, onDelete }: {
  item: MarketArcView; onEdit: () => void; onDelete: () => void
}) {
  return (
    <div className="group relative bg-surface border border-border rounded-xl p-4 hover:border-gold/40 transition-all">
      <div className="flex items-start justify-between gap-2 mb-2">
        <h3 className="font-display text-sm text-ink leading-tight flex-1">{item.name}</h3>
        <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button onClick={onEdit} className="text-xs font-body text-ink-muted hover:text-gold transition-colors px-2 py-1 rounded border border-transparent hover:border-border">
            Изм.
          </button>
          <button onClick={onDelete} className="p-1 rounded hover:text-crimson transition-colors">
            <Trash2 className="h-3.5 w-3.5 text-ink-muted hover:text-crimson" />
          </button>
        </div>
      </div>
      <div className="flex flex-wrap gap-1.5 mb-3">
        <span className="px-2 py-0.5 rounded-full bg-gold/10 border border-gold/20 text-xs text-gold">{item.brandName}</span>
        <span className="px-2 py-0.5 rounded-full bg-elevated border border-border text-xs text-ink-dim">{item.flavorName}</span>
        <span className="px-2 py-0.5 rounded-full bg-elevated border border-border text-xs text-ink-muted">{item.weightGrams} г</span>
        {item.gtin && (
          <span className="px-2 py-0.5 rounded-full bg-elevated border border-border text-xs text-ink-muted font-mono">
            {item.gtin}
          </span>
        )}
      </div>
      <p className="text-xs font-body text-ink-muted">
        {new Date(item.updatedAt).toLocaleDateString('ru-RU')}
      </p>
    </div>
  )
}

// ── MarketPage ──────────────────────────────────────────────────────────────
export default function MarketPage() {
  const [filters, setFilters] = useState({
    name: '', brandName: '', flavorName: '', weightMin: '', weightMax: '',
  })
  const [sortBy, setSortBy] = useState('updated_at')
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('desc')
  const [editItem, setEditItem] = useState<MarketArcView | null | 'new'>(null)
  const [deleteItem, setDeleteItem] = useState<MarketArcView | null>(null)

  const query = useInfiniteQuery({
    queryKey: ['market', filters, sortBy, sortDir],
    queryFn: async ({ pageParam }) => {
      const res = await marketApi.list({
        after: pageParam || undefined,
        limit: 20,
        name: filters.name || undefined,
        brandName: filters.brandName || undefined,
        flavorName: filters.flavorName || undefined,
        weightMin: filters.weightMin ? Number(filters.weightMin) : undefined,
        weightMax: filters.weightMax ? Number(filters.weightMax) : undefined,
        sortBy,
        sortDir,
      })
      return { data: res.data, nextCursor: res.headers['x-next-cursor'] || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })

  const items = query.data?.pages.flatMap(p => p.data) ?? []

  const toggleSort = (col: string) => {
    if (sortBy === col) setSortDir(d => d === 'asc' ? 'desc' : 'asc')
    else { setSortBy(col); setSortDir('desc') }
  }

  return (
    <div className="page-root">
      <div className="page-container page-enter">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-gold/10 border border-gold/30 flex items-center justify-center">
              <ShoppingBag className="h-4 w-4 text-gold" />
            </div>
            <div>
              <h1 className="font-display text-xl text-ink">Каталог рынка</h1>
              <p className="text-xs font-body text-ink-muted">Рыночные SKU для закупки</p>
            </div>
          </div>
          <button
            onClick={() => setEditItem('new')}
            className="flex items-center gap-2 px-3 py-2 rounded-lg bg-gold/10 border border-gold/30 text-gold text-sm font-body hover:bg-gold/20 transition-colors"
          >
            <Plus className="h-4 w-4" /> Добавить
          </button>
        </div>

        {/* Filters */}
        <div className="bg-surface border border-border rounded-xl p-4 mb-6 space-y-3">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            {[
              { key: 'name', placeholder: 'Название SKU...' },
              { key: 'brandName', placeholder: 'Бренд...' },
              { key: 'flavorName', placeholder: 'Вкус...' },
            ].map(({ key, placeholder }) => (
              <div key={key} className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-3.5 w-3.5 text-ink-muted" />
                <input
                  className="w-full pl-8 pr-3 py-2 rounded-lg border border-border bg-deep text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
                  placeholder={placeholder}
                  value={filters[key as keyof typeof filters]}
                  onChange={e => setFilters(f => ({ ...f, [key]: e.target.value }))}
                />
              </div>
            ))}
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex items-center gap-2">
              <input
                type="number" min={0} placeholder="Вес от"
                className="w-24 px-2 py-1.5 rounded-lg border border-border bg-deep text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
                value={filters.weightMin}
                onChange={e => setFilters(f => ({ ...f, weightMin: e.target.value }))}
              />
              <span className="text-xs text-ink-muted">—</span>
              <input
                type="number" min={0} placeholder="до (г)"
                className="w-24 px-2 py-1.5 rounded-lg border border-border bg-deep text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
                value={filters.weightMax}
                onChange={e => setFilters(f => ({ ...f, weightMax: e.target.value }))}
              />
            </div>
            {/* Sort */}
            <div className="flex items-center gap-2 ml-auto">
              <span className="text-xs text-ink-muted">Сортировка:</span>
              <select
                className="px-2 py-1.5 rounded-lg border border-border bg-deep text-sm font-body text-ink outline-none focus:border-gold transition-colors"
                value={sortBy}
                onChange={e => setSortBy(e.target.value)}
              >
                {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
              </select>
              <button
                onClick={() => setSortDir(d => d === 'asc' ? 'desc' : 'asc')}
                className="p-1.5 rounded-lg border border-border hover:bg-hover transition-colors"
              >
                {sortDir === 'asc'
                  ? <ChevronUp className="h-4 w-4 text-gold" />
                  : <ChevronDown className="h-4 w-4 text-gold" />}
              </button>
            </div>
          </div>
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {query.isLoading
            ? Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="h-28 rounded-xl bg-surface border border-border animate-pulse" />
              ))
            : items.length === 0
              ? (
                <div className="col-span-full flex flex-col items-center justify-center py-20 text-center">
                  <ShoppingBag className="h-12 w-12 text-ink-muted mb-4" />
                  <p className="font-display text-lg text-ink-dim">Нет позиций</p>
                  <p className="text-sm font-body text-ink-muted mt-1">Измените фильтры или добавьте SKU</p>
                </div>
              )
              : items.map(item => (
                  <MarketCard
                    key={item.id}
                    item={item}
                    onEdit={() => setEditItem(item)}
                    onDelete={() => setDeleteItem(item)}
                  />
                ))
          }
        </div>

        {/* Load more */}
        {query.hasNextPage && (
          <div className="flex justify-center mt-6">
            <button
              onClick={() => query.fetchNextPage()}
              disabled={query.isFetchingNextPage}
              className="px-6 py-2 rounded-lg border border-border text-sm font-body text-ink-dim hover:bg-hover transition-colors disabled:opacity-50"
            >
              {query.isFetchingNextPage ? 'Загрузка...' : 'Ещё'}
            </button>
          </div>
        )}

        {/* Dialogs */}
        {editItem && editItem !== 'new' && (
          <MarketFormDialog item={editItem} onClose={() => setEditItem(null)} />
        )}
        {editItem === 'new' && (
          <MarketFormDialog item={null} onClose={() => setEditItem(null)} />
        )}
        {deleteItem && (
          <DeleteMarketDialog item={deleteItem} onClose={() => setDeleteItem(null)} />
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: TypeScript lint**

```bash
cd frontend && npm run lint
```
Исправить ошибки типов если есть.

- [ ] **Step 3: git add + commit**

```bash
git add frontend/src/pages/admin/MarketPage.tsx
git commit -m "feat: add MarketPage with filters, sorting, infinite scroll, CRUD"
```

---

## Task 12: App.tsx + Navbar.tsx — добавить роуты и nav items

**Files:**
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/components/Navbar.tsx`

- [ ] **Step 1: Добавить импорты и роуты в App.tsx**

Добавить импорты:
```typescript
import FlavorsPage from '@/pages/admin/FlavorsPage'
import MarketPage  from '@/pages/admin/MarketPage'
```

Добавить роуты в `AppLayout`:
```tsx
<Route path="/admin/flavors" element={<FlavorsPage />} />
<Route path="/admin/market"  element={<MarketPage />} />
```

- [ ] **Step 2: Добавить nav items в Navbar.tsx**

Импортировать иконки — добавить `Flame` и `ShoppingBag` в импорт из `lucide-react` (Flame уже используется в логотипе).

Добавить в `NAV_ITEMS` после `{ to: '/admin/brands', ... }`:
```typescript
{ to: '/admin/flavors', label: 'Вкусы',   icon: Flame },
{ to: '/admin/market',  label: 'Рынок',    icon: ShoppingBag },
```

- [ ] **Step 3: TypeScript lint**

```bash
cd frontend && npm run lint
```

- [ ] **Step 4: Commit**

```bash
git add frontend/src/App.tsx frontend/src/components/Navbar.tsx
git commit -m "feat: add routes and nav items for FlavorsPage and MarketPage"
```

---

## Task 13: Smoke test

- [ ] **Step 1: Запустить бэкенд**

```bash
docker-compose up -d
./gradlew bootRun
```
Ожидаем: сервер поднялся на :8080

- [ ] **Step 2: Проверить новые эндпоинты**

```bash
# Получить access token сначала через POST /api/v1/auth/login
# Затем:
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/flavor?limit=5
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/flavor/search?limit=5
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/market?limit=5
```
Ожидаем: 200 OK с JSON массивами

- [ ] **Step 3: Запустить фронтенд**

```bash
cd frontend && npm run dev
```
Открыть http://localhost:3000

- [ ] **Step 4: Проверить UI**
  - `/admin/flavors` — выбрать бренд, убедиться что вкусы загружаются, создать вкус, добавить тег
  - `/admin/packs` — открыть форму создания/редактирования, убедиться что dropdown вкусов работает
  - `/admin/market` — создать SKU, проверить фильтры, сортировку, удаление

- [ ] **Step 5: Final commit если есть мелкие правки**

```bash
git add -A
git commit -m "fix: smoke test fixes"
```

---

## Контрольный список завершения

- [ ] `FlavorTagEvent` расширяет `FlavorTagEvent`, не `BrandEvent`
- [ ] `GET /api/v1/flavor` и `GET /api/v1/flavor/search` работают
- [ ] `GET /api/v1/market` поддерживает фильтры + сортировку
- [ ] `MarketService` + `MarketController` работают
- [ ] `FlavorsPage`: бренд-селектор → список вкусов, CRUD, теги
- [ ] `PacksPage`: dropdown вкусов в форме
- [ ] `MarketPage`: фильтры, сортировка, infinite scroll, CRUD
- [ ] CLAUDE.md обновлён с бизнес-контекстом market_arc
- [ ] Все новые файлы добавлены через `git add`
