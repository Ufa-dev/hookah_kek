# Brands Page — Tag Filtering

**Date:** 2026-03-22
**Status:** Approved

## Overview

Add server-side tag filtering (AND logic) to the brands catalog page, mirroring the existing flavor filtering pattern.

## Backend

### 1. `BrandsTagRepository`
Add method `findAllBrandIdsByAllTagIds(tagIds: List<TagId>): List<BrandId>` using SQL intersection (GROUP BY / HAVING COUNT = tagIds.size), same pattern as `findFlavorIdsByAllTagIds`.

### 2. `BrandService`
Add method `search(name: String?, tagIds: List<TagId>, cursor: BrandId?, limit: Int): Page<TabacoBrand>`:
- If `tagIds` non-empty: fetch matching `brandIds` via repository, filter by name within that set
- If `tagIds` empty: filter by name across all brands
- Return cursor-paginated results (X-Next-Cursor header)

### 3. `TabacoBrandController`
New endpoint: `GET /api/v1/brand/search`
Query params: `name?`, `tagIds[]?`, `cursor?`, `limit` (default 20)

## Frontend

### `api.ts`
Add `brandApi.search({ name?, tagIds?, cursor?, limit })` calling `GET /api/v1/brand/search`.

### `BrandsPage.tsx`
- Add `selectedTags: Tag[]` state
- Add tag filter UI (pills + add button) copied from `FlavorsPage` lines 348–381
- Switch `useInfiniteQuery` to use `brandApi.search(...)` with `queryKey: ['brands', search, tagIds]`
- Existing create/edit/delete functionality unchanged

## Data Flow

```
User selects tags → selectedTags state updates →
query key changes → infinite query refetches →
GET /brand/search?tagIds[]=1&tagIds[]=2 →
BrandService finds brandIds with ALL tags →
filters by name if provided → returns paginated brands
```

## Testing

- Manual: select 1 tag, verify only matching brands shown; select 2 tags, verify AND logic
- Manual: combine tag filter with name search
- Manual: clear tags, verify full list returns
