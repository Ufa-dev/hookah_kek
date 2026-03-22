# Brands Page — Tag Filtering

**Date:** 2026-03-22
**Status:** Approved

## Overview

Add server-side tag filtering (AND logic) to the brands catalog page. The pattern mirrors `FlavorService.search` / `FlavorController.search` / `FlavorsPage`.

---

## Backend

### 1. `BrandsTagRepository` — add `findAllBrandIdsByAllTagIds`

**Package:** `com.hookah.kek_hookah.feature.tobacco.brand.internal.repository`

Add method `findAllBrandIdsByAllTagIds(tagIds: List<TagId>): List<BrandId>`.

Exact body — copy `FlavorTagRepository.findFlavorIdsByAllTagIds` with brand types:

```kotlin
suspend fun findAllBrandIdsByAllTagIds(tagIds: List<TagId>): List<BrandId> {
    if (tagIds.isEmpty()) return emptyList()
    var result: Set<UUID>? = null
    for (tagId in tagIds) {
        val idsForTag = template.select(BrandTagEntity::class.java)
            .matching(Query.query(where("tag_id").`is`(tagId.id)))
            .all().collectList().awaitSingle()
            .map { it.brandId }.toSet()
        result = result?.intersect(idsForTag) ?: idsForTag
    }
    return (result ?: emptySet()).map { BrandId(it) }
}
```

The existing `findAllBrandIdsByTagIds` (OR logic, IN query) is unchanged.

---

### 2. `BrandRepository` — add `findAllByNameContaining`

**Package:** `com.hookah.kek_hookah.feature.tobacco.brand.internal.repository`

Add method `findAllByNameContaining(name: String, afterId: UUID?, limit: Int): List<TabacoBrand>`.

Mirrors `findAll(limit, afterId)` with an added name filter using `LIKE '%name%'` (the `name` column is `CITEXT` so case-insensitive natively):

```kotlin
suspend fun findAllByNameContaining(name: String, afterId: UUID?, limit: Int): List<TabacoBrand> {
    val criteria = where("name").like("%$name%")
        .let { if (afterId != null) it.and(where("id").greaterThan(afterId)) else it }
    return template.select(BrandEntity::class.java)
        .matching(Query.query(criteria).sort(Sort.by(Sort.Direction.ASC, "id")).limit(limit))
        .all().collectList().awaitSingle().map { it.toBrand() }
}
```

---

### 3. `SearchBrandsQuery` — new use-case class

**Package:** `com.hookah.kek_hookah.feature.tobacco.brand.internal.usecase`

New `@Component` class `SearchBrandsQuery(brandRepository, brandsTagRepository, tagService)`.

```kotlin
@Component
class SearchBrandsQuery(
    private val brandRepository: BrandRepository,
    private val brandsTagRepository: BrandsTagRepository,
    private val tagService: TagService,
) {
    suspend fun execute(name: String?, tagIds: List<TagId>, afterId: UUID?, limit: Int): List<TabacoBrand> {
        val brandIdFilter: Set<UUID>? = if (tagIds.isNotEmpty())
            brandsTagRepository.findAllBrandIdsByAllTagIds(tagIds).map { it.id }.toSet()
        else null

        if (brandIdFilter != null && brandIdFilter.isEmpty()) return emptyList()

        val base: List<TabacoBrand> = if (!name.isNullOrBlank())
            brandRepository.findAllByNameContaining(name, afterId, limit)
        else
            brandRepository.findAll(limit, afterId)

        val enriched = base.map { brand ->
            val tagUuids = brandsTagRepository.findAllByBrandId(brand.id)   // returns List<UUID>
            val tags = tagUuids.mapNotNull { tagService.findById(TagId(it)) }
            brand.copy(tags = tags)
        }

        return if (brandIdFilter != null) enriched.filter { it.id.id in brandIdFilter } else enriched
    }
}
```

**Pagination note:** `base` is fetched at most `limit` rows, then filtered by `brandIdFilter` — effective page size may be smaller. Accepted limitation, consistent with flavor pattern.

---

### 4. `BrandService` — add `search` method

Inject `SearchBrandsQuery` into `BrandService` and add:

```kotlin
suspend fun search(name: String?, tagIds: List<TagId>, afterId: UUID?, limit: Int): List<TabacoBrand> {
    return searchBrandsQuery.execute(name, tagIds, afterId, limit)
}
```

---

### 5. `TabacoBrandController` — new endpoint

```kotlin
@GetMapping("/search")
suspend fun search(
    @RequestParam(required = false) name: String?,
    @RequestParam(required = false) tagIds: List<UUID>?,
    @RequestParam(required = false) cursor: UUID?,
    @RequestParam(defaultValue = "20") limit: Int,
    response: ServerHttpResponse,
): List<TabacoBrand> {
    val result = service.search(
        name = name,
        tagIds = tagIds?.map { TagId(it) } ?: emptyList(),
        afterId = cursor,
        limit = limit,
    )
    response.headers["X-Next-Cursor"] = result.lastOrNull()?.id?.id?.toString() ?: ""
    return result
}
```

`X-Next-Cursor` is empty string `""` when there are no more results (matches `FlavorController.search` behavior; frontend treats empty string as no next page).

---

## Frontend

### `api.ts` — add `brandApi.search`

```typescript
search: (params: { name?: string; tagIds?: string[]; cursor?: string; limit?: number }) =>
  http.get<TabacoBrand[]>('/brand/search', { params: { ...params, limit: params.limit ?? 20 } }),
```

**Critical:** no `.then(r => r.data)` — returns the raw axios response so the caller can read `res.headers['x-next-cursor']`. Consistent with `flavorApi.search`.

---

### `BrandsPage.tsx` — changes

#### State
```typescript
const [selectedTags, setSelectedTags] = useState<Tag[]>([])
const tagIds = selectedTags.map(t => t.id)
```

#### `useInfiniteQuery` for all tags (add at top level)
```typescript
const { data: tagsData } = useInfiniteQuery({
  queryKey: ['all-tags'],
  queryFn: async ({ pageParam }) => {
    const res = await tagApi.list({ limit: 100, after: pageParam || undefined })
    return { data: res.items, nextCursor: res.nextToken || '' }
  },
  getNextPageParam: (last) => last.nextCursor || undefined,
  initialPageParam: '',
})
const allTags: Tag[] = tagsData?.pages.flatMap(p => p.data) ?? []
```
Uses key `['all-tags']` with the **same queryFn shape** as `FlavorsPage` (`{ data, nextCursor }`) so the cache is shared when both pages are mounted. Note: `BrandTagsDialog` uses a separate key `['tags-infinite']` — no collision there.

#### Replace dual-query with single `useInfiniteQuery`
Remove:
- `useQuery(['brands-search', search])` + `brandApi.findByName` (search mode)
- `useInfiniteQuery(['brands-infinite'])` + `brandApi.list` (browse mode)
- `isSearchMode` branching

Add:
```typescript
const query = useInfiniteQuery({
  queryKey: ['brands', search, tagIds],
  queryFn: async ({ pageParam }) => {
    const res = await brandApi.search({
      name: search || undefined,
      tagIds: tagIds.length ? tagIds : undefined,
      cursor: pageParam || undefined,
      limit: PAGE_LIMIT,
    })
    return { data: res.data, nextCursor: res.headers['x-next-cursor'] || '' }
  },
  getNextPageParam: (last) => last.nextCursor || undefined,
  initialPageParam: '',
})
const brands: TabacoBrand[] = query.data?.pages.flatMap(p => p.data) ?? []
```

Keep the existing `IntersectionObserver` sentinel wired to `query.fetchNextPage` for auto-load.

#### Tag filter UI
Add below the search input — copy the tag pills + add-button from `FlavorsPage` lines 348–381:
- Each selected tag rendered as a pill with name + `✕` button (`onClick: remove tag from selectedTags`)
- `+` button opens dropdown showing tags from `allTags` excluding already-selected ones
- Clicking a tag in dropdown adds it to `selectedTags`

#### Mutation cache key updates (critical)
In `BrandFormDialog` and `DeleteBrandDialog`, change all:
```typescript
qc.invalidateQueries({ queryKey: ['brands-infinite'] })
```
to:
```typescript
qc.invalidateQueries({ queryKey: ['brands'] })
```

#### `BrandTagsDialog.syncCache` fix (critical)
Current `syncCache`:
```typescript
const syncCache = (updated: TabacoBrand) => {
  setLocalTags(updated.tags)                          // ← keep this line
  qc.setQueryData(['brands-infinite'], ...)           // ← replace this line
}
```
Replace only the `setQueryData` line with:
```typescript
qc.invalidateQueries({ queryKey: ['brands'] })
```
`setLocalTags(updated.tags)` is retained for immediate optimistic dialog state.

---

## Edge Cases

| Scenario | Behaviour |
|---|---|
| No tags, no name (initial load) | `brandApi.search({})` → all brands |
| Name only | Name filter, no tag constraint |
| Tags only | AND-intersection, all names |
| Name + tags | AND-intersection filtered by name |
| Tag deselected (all cleared) | Query key updates, refetches full list |
| Zero matches from AND filter | Returns `[]` immediately (early return) |
| Last page | `X-Next-Cursor: ""` → `getNextPageParam` returns `undefined` |
| Create/edit/delete while filter active | `invalidateQueries(['brands'])` refetches current filter |

---

## Testing

- Select 1 tag → only brands with that tag shown
- Select 2 tags → AND logic, only brands with both tags shown
- Combine tag filter with name search → both constraints applied
- Clear all tags → full list returns, infinite scroll resumes
- Infinite scroll works while tag filter is active
- Create/edit/delete brand refreshes list correctly while filter is active
- Tag add/remove in `BrandTagsDialog` refreshes brand tags in list
