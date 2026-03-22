import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Search, X, ChevronDown } from 'lucide-react'
import { flavorApi, brandApi, tagApi } from '@/lib/api'
import type { TabacoFlavor, TabacoBrand, Tag, FlavorCreateRequest, FlavorUpdateRequest } from '@/types'
import { FlavorCard } from '@/components/cards'
import { BrandSelector } from '@/components/ui/BrandSelector'
import { AddButton } from '@/components/ui/AddButton'
import { PageHeader } from '@/components/ui/PageHeader'
import { TagSelector } from '@/components/ui/TagSelector'

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
      <div className="bg-white border border-border rounded-xl p-6 w-full max-w-md shadow-2xl" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-display text-base text-ink">Теги: {flavor.name}</h2>
          <button onClick={onClose}><X className="h-4 w-4 text-ink-muted hover:text-red" /></button>
        </div>
        <TagSelector
          assigned={tags}
          allTags={allTags}
          onAdd={t => addMut.mutate(t)}
          onRemove={t => removeMut.mutate(t)}
        />
      </div>
    </div>
  )
}

// ── BrandDropdown (inline в форме) ───────────────────────────────────────────
function BrandDropdown({ selected, allBrands, onSelect }: {
  selected: TabacoBrand | null
  allBrands: TabacoBrand[]
  onSelect: (b: TabacoBrand) => void
}) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const h = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  const filtered = allBrands.filter(b => b.name.toLowerCase().includes(query.toLowerCase()))

  return (
    <div ref={ref} className="relative">
      <button
        type="button"
        onClick={() => setOpen(o => !o)}
        className="w-full flex items-center justify-between px-3 py-2 rounded-lg border border-border bg-elevated text-sm font-body text-left hover:border-red transition-colors"
      >
        <span className={selected ? 'text-ink' : 'text-ink-muted'}>
          {selected ? selected.name : 'Выберите бренд...'}
        </span>
        <ChevronDown className="h-3.5 w-3.5 text-ink-muted flex-shrink-0" />
      </button>
      {open && (
        <div className="absolute z-50 top-full mt-1 w-full bg-white border border-border rounded-lg shadow-lg">
          <div className="p-2 border-b border-border">
            <input
              autoFocus
              className="w-full bg-transparent text-xs font-body text-ink placeholder:text-ink-muted outline-none"
              placeholder="Поиск..."
              value={query}
              onChange={e => setQuery(e.target.value)}
            />
          </div>
          <div className="max-h-40 overflow-y-auto">
            {filtered.length === 0
              ? <p className="px-3 py-2 text-xs text-ink-muted">Не найдено</p>
              : filtered.map(b => (
                <button
                  key={b.id}
                  type="button"
                  className="w-full text-left px-3 py-2 text-sm font-body text-ink hover:bg-elevated transition-colors"
                  onClick={() => { onSelect(b); setOpen(false); setQuery('') }}
                >
                  {b.name}
                </button>
              ))
            }
          </div>
        </div>
      )}
    </div>
  )
}

// ── FlavorFormDialog ────────────────────────────────────────────────────────
function FlavorFormDialog({ flavor, brand, allBrands, onClose }: {
  flavor: TabacoFlavor | null
  brand: TabacoBrand | null
  allBrands: TabacoBrand[]
  onClose: () => void
}) {
  const qc = useQueryClient()
  const [formBrand, setFormBrand] = useState<TabacoBrand | null>(
    flavor ? allBrands.find(b => b.id === flavor.brandId) ?? brand : brand
  )
  const [form, setForm] = useState({
    name: flavor?.name ?? '',
    description: flavor?.description ?? '',
    strength: flavor?.strength ?? 5,
  })

  const mut = useMutation({
    mutationFn: () => {
      const body: FlavorCreateRequest | FlavorUpdateRequest = {
        brandId: formBrand!.id,
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
      <div className="bg-white border border-border rounded-xl p-6 w-full max-w-md shadow-2xl" onClick={e => e.stopPropagation()}>
        <div className="flex items-center justify-between mb-5">
          <h2 className="font-display text-base text-ink">{flavor ? 'Редактировать вкус' : 'Новый вкус'}</h2>
          <button onClick={onClose}><X className="h-4 w-4 text-ink-muted hover:text-red" /></button>
        </div>
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1 font-medium">Бренд *</label>
            <BrandDropdown selected={formBrand} allBrands={allBrands} onSelect={setFormBrand} />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1 font-medium">Название *</label>
            <input
              className="w-full px-3 py-2 rounded-lg border border-border bg-elevated text-sm font-body text-ink placeholder:text-ink-muted outline-none focus:border-red transition-colors"
              value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
              placeholder="Название вкуса"
            />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1 font-medium">Описание</label>
            <textarea
              rows={2}
              className="w-full px-3 py-2 rounded-lg border border-border bg-elevated text-sm font-body text-ink placeholder:text-ink-muted outline-none focus:border-red transition-colors resize-none"
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              placeholder="Описание вкуса"
            />
          </div>
          <div>
            <label className="block text-xs font-body text-ink-muted mb-1 font-medium">
              Крепость:{' '}
              <span className="font-display text-base text-red">
                {form.strength}
              </span>
              {' '}/10
            </label>
            <input
              type="range" min={0} max={10} step={1}
              className="strength-slider w-full"
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
            disabled={!form.name.trim() || !formBrand || mut.isPending}
            className="px-4 py-2 rounded-lg text-sm font-body font-semibold bg-[#9B2335] text-white hover:bg-[#B91C1C] disabled:opacity-50 transition-colors"
          >
            {mut.isPending ? 'Сохранение...' : (flavor ? 'Сохранить' : 'Создать')}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── DeleteFlavorDialog ───────────────────────────────────────────────────────
function DeleteFlavorDialog({ flavor, isOpen, onClose }: { flavor: TabacoFlavor; isOpen: boolean; onClose: () => void }) {
  const qc = useQueryClient()
  const deleteMut = useMutation({
    mutationFn: () => flavorApi.delete(flavor.id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['flavors'] })
      onClose()
      toast.success('Вкус удалён')
    },
    onError: () => toast.error('Не удалось удалить вкус'),
  })
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4" onClick={onClose}>
      <div className="bg-surface border border-border rounded-xl p-6 w-full max-w-sm shadow-2xl" onClick={e => e.stopPropagation()}>
        <h2 className="font-display text-base text-ink mb-2">Удалить вкус?</h2>
        <p className="text-sm font-body text-ink-dim mb-5">«{flavor.name}» будет удалён без возможности восстановления.</p>
        <div className="flex justify-end gap-3">
          <button onClick={onClose} className="px-4 py-2 rounded-lg text-sm font-body text-ink-dim border border-border hover:bg-hover transition-colors">Отмена</button>
          <button
            onClick={() => deleteMut.mutate()}
            disabled={deleteMut.isPending}
            className="px-4 py-2 rounded-lg text-sm font-body font-medium bg-red/10 border border-red/30 text-red hover:bg-red/20 disabled:opacity-50 transition-colors"
          >
            {deleteMut.isPending ? 'Удаление...' : 'Удалить'}
          </button>
        </div>
      </div>
    </div>
  )
}

// ── FlavorsPage ──────────────────────────────────────────────────────────────
export default function FlavorsPage() {
  const [selectedBrand, setSelectedBrand] = useState<TabacoBrand | null>(null)
  const [search, setSearch] = useState('')
  const [editFlavor, setEditFlavor] = useState<TabacoFlavor | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [deleteFlavor, setDeleteFlavor] = useState<TabacoFlavor | null>(null)
  const [tagsFlavor, setTagsFlavor] = useState<TabacoFlavor | null>(null)
  const [selectedTags, setSelectedTags] = useState<Tag[]>([])
  const [tagFilterOpen, setTagFilterOpen] = useState(false)
  const tagFilterRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const h = (e: MouseEvent) => { if (tagFilterRef.current && !tagFilterRef.current.contains(e.target as Node)) setTagFilterOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  // All tags via tagApi.list
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

  // All brands (для дропдауна в форме)
  const { data: brandsData } = useInfiniteQuery({
    queryKey: ['brands-for-form'],
    queryFn: async ({ pageParam }) => {
      const res = await brandApi.list({ limit: 100, after: pageParam || undefined })
      return { data: res.items, nextCursor: res.nextToken || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })
  const allBrands: TabacoBrand[] = brandsData?.pages.flatMap(p => p.data) ?? []

  const tagIds = selectedTags.map(t => t.id)

  const query = useInfiniteQuery({
    queryKey: ['flavors', selectedBrand?.id, search, tagIds],
    queryFn: async ({ pageParam }) => {
      const res = await flavorApi.search({
        brandId: selectedBrand?.id,
        name: search || undefined,
        tagIds: tagIds.length ? tagIds : undefined,
        cursor: pageParam || undefined,
        limit: 20,
      })
      return { data: res.data, nextCursor: res.headers['x-next-cursor'] || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })

  const flavors = query.data?.pages.flatMap(p => p.data) ?? []

  return (
    <div className="page-root">
      <div className="page-container page-enter">
        <PageHeader
          title="Вкусы"
          onAdd={() => { setEditFlavor(null); setShowForm(true) }}
        />

        {/* Brand Selector + Search */}
        <div className="flex flex-col sm:flex-row gap-3 mb-6">
          <BrandSelector selected={selectedBrand} onSelect={b => { setSelectedBrand(b); setSearch('') }} />
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted" />
            <input
              className="w-full pl-9 pr-3 py-2 rounded-lg border border-border bg-surface text-sm font-body text-ink placeholder-ink-muted outline-none focus:border-gold transition-colors"
              placeholder="Поиск по названию..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
          </div>
        </div>

        {/* Tag filter */}
        <div className="flex flex-wrap gap-2 items-center mb-6">
          <span className="text-xs text-ink-muted font-body">Теги:</span>
          {selectedTags.map(t => (
            <span key={t.id} className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-gold/10 border border-gold/30 text-xs text-gold">
              {t.name}
              <button onClick={() => setSelectedTags(prev => prev.filter(st => st.id !== t.id))}>
                <X className="h-2.5 w-2.5 hover:text-crimson" />
              </button>
            </span>
          ))}
          <div ref={tagFilterRef} className="relative">
            <button
              onClick={() => setTagFilterOpen(o => !o)}
              className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border border-dashed border-border text-xs text-ink-dim hover:border-gold hover:text-gold transition-colors"
            >
              <Plus className="h-3 w-3" /> Тег
            </button>
            {tagFilterOpen && (
              <div className="absolute z-50 top-full mt-1 w-48 bg-white border border-border rounded-lg shadow-lg">
                <div className="max-h-40 overflow-y-auto">
                  {allTags.filter(t => !selectedTags.find(st => st.id === t.id)).map(t => (
                    <button key={t.id}
                      className="w-full text-left px-3 py-1.5 text-xs text-ink hover:bg-elevated transition-colors"
                      onClick={() => setSelectedTags(prev => [...prev, t])}
                    >
                      {t.name}
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {query.isLoading
            ? Array.from({ length: 6 }).map((_, i) => (
                <div key={i} className="h-32 rounded-xl bg-surface border border-border animate-pulse" />
              ))
            : flavors.map(f => (
                <FlavorCard
                  key={f.id}
                  flavor={f}
                  onEdit={() => { setEditFlavor(f); setShowForm(true) }}
                  onManageTags={() => setTagsFlavor(f)}
                  onDelete={() => setDeleteFlavor(f)}
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

        {/* Form dialog */}
        {showForm && (
          <FlavorFormDialog
            flavor={editFlavor}
            brand={editFlavor ? allBrands.find(b => b.id === editFlavor.brandId) ?? selectedBrand : selectedBrand}
            allBrands={allBrands}
            onClose={() => setShowForm(false)}
          />
        )}

        {/* Tags dialog */}
        {tagsFlavor && (
          <FlavorTagsDialog flavor={tagsFlavor} allTags={allTags} onClose={() => setTagsFlavor(null)} />
        )}

        {/* Delete dialog */}
        {deleteFlavor && (
          <DeleteFlavorDialog flavor={deleteFlavor} isOpen={!!deleteFlavor} onClose={() => setDeleteFlavor(null)} />
        )}
      </div>
    </div>
  )
}
