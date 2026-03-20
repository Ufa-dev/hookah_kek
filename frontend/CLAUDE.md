# frontend/CLAUDE.md

Frontend-specific guidance for Claude Code. Read the root `CLAUDE.md` first for project context and domain overview.

---

## Directory structure

```
frontend/src/
├── components/
│   ├── cards/          # Shared domain card components (one per entity)
│   │   ├── BrandCard.tsx
│   │   ├── FlavorCard.tsx
│   │   ├── PackCard.tsx
│   │   ├── MarketCard.tsx
│   │   └── index.ts    # barrel re-export — always import from here
│   ├── ui/             # Primitive UI components (shadcn/ui pattern, hand-rolled)
│   │   ├── badge.tsx
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── dialog.tsx
│   │   ├── input.tsx
│   │   ├── label.tsx
│   │   ├── textarea.tsx
│   │   └── BrandSelector.tsx   # Reusable brand search/select dropdown
│   ├── Navbar.tsx
│   └── ProtectedRoute.tsx
├── contexts/
│   ├── AuthContext.tsx          # login/register/logout + token management
│   └── ThemeContext.tsx         # light/dark toggle, persisted to localStorage
├── lib/
│   ├── api.ts                   # All API calls (authApi, brandApi, …)
│   ├── queryClient.ts
│   └── utils.ts                 # cn(), formatDate()
├── pages/
│   ├── admin/
│   │   ├── BrandsPage.tsx
│   │   ├── FlavorsPage.tsx
│   │   ├── PacksPage.tsx
│   │   ├── MarketPage.tsx
│   │   └── TagsPage.tsx
│   └── DashboardPage.tsx
└── types/index.ts               # All TypeScript types matching backend DTOs
```

---

## Card component pattern

All domain cards share the same structure. **Do not define card components inline in pages** — put them in `components/cards/`.

### Standard card anatomy

```tsx
// container
<div className="card group flex flex-col hover:border-red-light transition-colors duration-150">

  {/* body — flex-1 so footer sticks to bottom */}
  <div className="p-4 sm:p-5 flex-1">
    <div className="flex items-start justify-between gap-2 mb-2">
      <h3 className="font-display text-lg sm:text-xl text-ink leading-snug">{title}</h3>
      {/* edit pencil — revealed on hover */}
      <button
        onClick={onEdit}
        className="opacity-0 group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-elevated text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center"
        aria-label="Редактировать"
      >
        <Pencil className="h-3.5 w-3.5" />
      </button>
    </div>
    {/* optional description */}
    {description && <p className="text-sm text-ink-dim mb-3 line-clamp-2">{description}</p>}
    {/* tags */}
    <div className="flex flex-wrap gap-1.5 mb-3 min-h-[26px]">
      {tags.map(tag => <Badge key={tag.id}><TagIcon className="h-2.5 w-2.5" />{tag.name}</Badge>)}
    </div>
  </div>

  {/* footer */}
  <div className="border-t border-border px-4 sm:px-5 py-3 flex items-center justify-between gap-2">
    {/* left: timestamp */}
    <div className="flex items-center gap-1.5 text-xs text-ink-muted min-w-0">
      <Clock className="h-3 w-3 flex-shrink-0" />
      <span className="truncate">{formatDate(updatedAt)}</span>
    </div>
    {/* right: actions + delete (revealed on hover) */}
    <div className="flex items-center gap-2 flex-shrink-0">
      <button onClick={onAction} className="flex items-center gap-1 text-xs font-body font-medium text-red hover:text-red-dim transition-colors touch-target">
        <SomeIcon className="h-3.5 w-3.5" /> Label
      </button>
      <button
        onClick={onDelete}
        className="opacity-0 group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-elevated text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center"
        aria-label="Удалить"
      >
        <Trash2 className="h-3.5 w-3.5" />
      </button>
    </div>
  </div>
</div>
```

### Rules
- Edit button and delete button are **hidden by default** (`opacity-0`) and revealed on `group-hover` and `focus`.
- Tags always use `<Badge>` (default `red` variant). Never style tags with inline gold/yellow styles.
- `<Badge variant="surface">` for neutral labels (brand name, category). `<Badge variant="outline">` for secondary metadata (weight, GTIN). `<Badge>` (default) for tags.
- Cards always import from `@/components/cards` (barrel).

---

## Badge variants

```ts
// badge.tsx variants:
red     // default — crimson pill, used for tags
outline // transparent bg, border only — secondary metadata
surface // elevated bg — neutral entity labels
```

---

## Button variants

```ts
// button.tsx variants:
primary     // bg-red — main CTA
ghost       // text-red, border on hover
outline     // border, text-ink-dim, red on hover
danger      // border-red-glow, text-red, bg-transparent — delete confirm
link        // text-red underline, no padding
```

**Gotcha**: There is **no** `destructive` variant — use `danger`.

---

## Design system tokens (Tailwind)

Custom tokens defined in `tailwind.config.ts`:

| Token | Usage |
|---|---|
| `text-ink` | Primary text |
| `text-ink-dim` | Secondary text |
| `text-ink-muted` | Placeholder/meta text |
| `bg-surface` | Page background |
| `bg-elevated` | Card/panel background |
| `bg-hover` | Row hover state |
| `border-border` | Default border |
| `text-red` / `bg-red` | Crimson accent (#9B2335) |
| `text-red-dim` | Darker crimson |
| `border-red-light` | Card hover border |
| `bg-red-pale` | Ghost/hover background |
| `border-red-glow` | Subtle red border |
| `text-gold` / `bg-gold` | Gold accent (#D4A647) — use sparingly |
| `font-display` | Cinzel — headings only |
| `font-body` | Outfit — body/UI text |

CSS class shorthands (defined in `index.css`):

| Class | Meaning |
|---|---|
| `.card` | Standard card container with border, rounded corners, bg |
| `.card-hover` | Card with hover lift shadow |
| `.page-root` | Full-height page wrapper |
| `.page-container` | Max-width content container with padding |
| `.page-enter` | Fade-in animation |
| `.field` | Form field container |
| `.touch-target` | Minimum 48px touch area |

---

## Dark theme ("Кровавый уголь")

Theme is stored as `'light' | 'dark'` in `localStorage` under key `kek_theme`.

`ThemeProvider` (in `contexts/ThemeContext.tsx`) toggles the `dark` class on `<html>`. Tailwind `darkMode: ['class']` activates dark variants.

### Adding dark support to new components

Two approaches:

**1. Tailwind `dark:` utilities** — preferred for Navbar and structural elements:
```tsx
<div className="bg-white dark:bg-[#111111] border-border dark:border-[#2a2a2a]">
```

**2. CSS class overrides** — for global design tokens in `index.css`:
```css
.dark .card {
  background: #161616;
  border-color: #2a2a2a;
  border-top: 2px solid #B91C1C;
}
```

Dark palette:
- `#0d0d0d` — body/void background
- `#111111` — navbar/sidebar
- `#161616` — card surface
- `#2a2a2a` — borders
- `#B91C1C` — card top accent border (red)
- `#f5f5f5` — primary text

### useTheme hook
```tsx
import { useTheme } from '@/contexts/ThemeContext'

const { theme, toggleTheme } = useTheme()
// theme: 'light' | 'dark'
// toggleTheme(): void
```

---

## TanStack Query v5 patterns

### Infinite scroll (cursor-based)

```tsx
const { data, fetchNextPage, hasNextPage, isFetchingNextPage } = useInfiniteQuery({
  queryKey: ['brands-infinite', search],
  queryFn: async ({ pageParam }) =>
    brandApi.list({ limit: PAGE_LIMIT, after: pageParam || undefined }),
  getNextPageParam: (last) => last.nextToken || undefined,
  initialPageParam: '',
})

const items = data?.pages.flatMap(p => p.items) ?? []
```

- Cursor is `nextToken` (string) from `Slice<T>` response.
- `initialPageParam` must be `''` (empty string), not `undefined`.
- Use `after: pageParam || undefined` to avoid sending `after=` as empty query param.

### Mutations + cache invalidation

```tsx
const deleteMut = useMutation({
  mutationFn: (id: string) => brandApi.delete(id),
  onSuccess: () => {
    qc.invalidateQueries({ queryKey: ['brands-infinite'] })
    toast.success('Бренд удалён')
    setDeleteBrand(null)
  },
  onError: () => toast.error('Не удалось удалить'),
})
```

- Always invalidate the **full query key** (including list key) on mutation success.
- `toast` comes from `sonner` — `toast.success()` / `toast.error()`.

### Query key conventions

| Entity | List key | Detail key |
|---|---|---|
| brands | `['brands-infinite']` | `['brand', id]` |
| flavors | `['flavors-infinite']` | — |
| packs | `['packs-infinite']` | `['pack', id]` |
| market | `['market-infinite']` | — |
| tags | `['tags-infinite']` | `['tag', id]` |
| BrandSelector | `['brands-selector', query]` | — |

---

## BrandSelector

Reusable combobox for selecting a brand. Used in FlavorsPage filter and PacksPage form.

```tsx
import { BrandSelector } from '@/components/ui/BrandSelector'

const [selectedBrand, setSelectedBrand] = useState<TabacoBrand | null>(null)

<BrandSelector selected={selectedBrand} onSelect={setSelectedBrand} />
```

- Shows brands list via `brandApi.list`; switches to `brandApi.findByName` when query ≥ 2 chars.
- Closes on outside click.
- When brand is cleared, pass `null` to `onSelect`.

### Pack form: Brand → Flavor cascade

`PackFormDialog` in `PacksPage.tsx` uses `BrandSelector` to filter flavors:
1. User picks a brand → `selectedBrand` is set, `flavorId` is reset to `''`.
2. `FlavorSelector` receives `brandId={selectedBrand?.id}`.
3. When `brandId` is set, `FlavorSelector` calls `flavorApi.findByBrandId(brandId, ...)` instead of `flavorApi.search(...)`.

---

## API client patterns (`src/lib/api.ts`)

All API modules follow the same shape:

```ts
export const brandApi = {
  list:     (params) => http.get<Slice<T>>('/brand', { params }).then(r => r.data),
  findById: (id)     => http.get<T>(`/brand/id/${id}`).then(r => r.data),
  create:   (body)   => http.post<T>('/brand', body).then(r => r.data),
  update:   (id, b)  => http.put<T>(`/brand/${id}`, b).then(r => r.data),
  delete:   (id)     => http.delete(`/brand/${id}`),
}
```

- `flavorApi` and `marketApi` return raw `AxiosResponse` (not `.then(r => r.data)`) for list/create/update — check the actual signatures before using `.data`.
- `packApi` uses `encodeURIComponent(id)` because pack IDs are user-defined strings (VARCHAR, may contain spaces).
- Auth: Bearer token injected by Axios interceptor; 401 triggers auto-refresh (single in-flight promise — no race conditions).

---

## Page layout boilerplate

```tsx
export default function FooPage() {
  return (
    <div className="page-root">
      <div className="page-container page-enter">
        {/* header row */}
        <div className="flex items-center justify-between mb-6">
          <h1 className="font-display text-2xl text-ink">Title</h1>
          <Button onClick={openCreate}><Plus className="h-4 w-4" /> Добавить</Button>
        </div>

        {/* filters row */}
        <div className="flex flex-wrap gap-3 mb-6">...</div>

        {/* grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map(item => <FooCard key={item.id} item={item} onEdit={...} onDelete={...} />)}
        </div>

        {/* load more */}
        {hasNextPage && (
          <div className="mt-6 text-center">
            <Button variant="outline" onClick={() => fetchNextPage()} disabled={isFetchingNextPage}>
              {isFetchingNextPage ? 'Загрузка…' : 'Ещё'}
            </Button>
          </div>
        )}
      </div>

      {/* dialogs */}
      <FooFormDialog ... />
      <DeleteFooDialog ... />
    </div>
  )
}
```

---

## Common gotchas

### Tags
- No `GET /tag` (list all) endpoint. Tags are discovered via brands (`brandApi.findByTags` / embedded in brand responses).
- `tagApi.findByName` is exact-match only — it throws 404 if not found.
- To add a tag: search by name first; if not found, create it in TagsPage, then add to entity.
- Tags always render as `<Badge>` (default red variant) with a `<TagIcon>` prefix icon.

### Brand/Flavor IDs
- `BrandId`, `FlavorId`, `TagId` are Kotlin value classes — they serialize as plain UUID strings. Type them as `string` in TypeScript.
- `PackId` wraps `String` (not UUID) — user-defined label up to 100 chars. Always `encodeURIComponent` in URLs.

### Flavor search vs findByBrandId
- `flavorApi.search({ name?, brandId? })` — general search, supports partial name match.
- `flavorApi.findByBrandId(brandId)` — all flavors for a brand, cursor-paginated.
- In the pack form: use `findByBrandId` (not `search`) when a brand is selected, so the user sees all flavors for that brand without needing to type.

### Form state sync
When a dialog receives an optional `brand?: TabacoBrand` prop for edit mode, sync local state using a pattern like:
```tsx
const [synced, setSynced] = useState(brand)
if (brand !== synced) { setSynced(brand); setName(brand?.name ?? '') }
```
This avoids stale state when the same dialog is reused for create and edit.

### `variant="destructive"` does not exist
Button has variants: `primary`, `ghost`, `outline`, `danger`, `link`. Use `danger` for delete confirm buttons.

### Mobile
- Bottom tab bar is `md:hidden` — only shown on mobile.
- Use `.touch-target` class on interactive elements to ensure ≥ 48px tap area.
- `min-h-dvh` (not `min-h-screen`) for full-height layouts to account for mobile browser chrome.
