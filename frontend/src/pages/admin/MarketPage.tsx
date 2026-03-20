import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Search, X, ChevronDown, ShoppingBag, ChevronUp } from 'lucide-react'
import { marketApi, brandApi, flavorApi } from '@/lib/api'
import { MarketCard } from '@/components/cards'
import type { MarketArcView, MarketCreateRequest, MarketUpdateRequest, TabacoBrand, TabacoFlavor } from '@/types'

const SORT_OPTIONS = [
  { value: 'updated_at', label: 'Дата обновления' },
  { value: 'name', label: 'Название' },
  { value: 'brand_name', label: 'Бренд' },
  { value: 'flavor_name', label: 'Вкус' },
  { value: 'weight_grams', label: 'Вес' },
]

// ── BrandSelectorCompact ───────────────────────────────────────────────────
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
        return { data: Array.isArray(res) ? res : [res], nextCursor: '' }
      }
      const res = await brandApi.list({ limit: 20, after: pageParam || undefined })
      return { data: res.items, nextCursor: res.nextToken || '' }
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
    onError: (e: Error) => toast.error(e.message || 'Ошибка при сохранении'),
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
