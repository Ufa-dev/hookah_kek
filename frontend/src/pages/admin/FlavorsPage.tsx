import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Search, X, ChevronDown, Flame, Tag as TagIcon } from 'lucide-react'
import { flavorApi, brandApi } from '@/lib/api'
import type { TabacoFlavor, TabacoBrand, Tag, FlavorCreateRequest, FlavorUpdateRequest } from '@/types'

// ── BrandSelector ──────────────────────────────────────────────────────────
function BrandSelector({
  selected, onSelect,
}: { selected: TabacoBrand | null; onSelect: (b: TabacoBrand | null) => void }) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  const { data: brands } = useInfiniteQuery({
    queryKey: ['brands-selector', query],
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
          <button onClick={e => { e.stopPropagation(); onSelect(null); setQuery('') }}>
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
  const [editFlavor, setEditFlavor] = useState<TabacoFlavor | null>(null)
  const [showForm, setShowForm] = useState(false)

  // All tags (для TagDropdown) — берём из брендов
  const { data: brandsData } = useInfiniteQuery({
    queryKey: ['brands-for-tags'],
    queryFn: async ({ pageParam }) => {
      const res = await brandApi.list({ limit: 100, after: pageParam || undefined })
      return { data: res.items, nextCursor: res.nextToken || '' }
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
            flavor={editFlavor}
            brand={selectedBrand}
            onClose={() => setShowForm(false)}
          />
        )}
      </div>
    </div>
  )
}
