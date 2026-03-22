import { useState, useEffect, useRef, type FormEvent } from 'react'
import { useInfiniteQuery, useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { brandApi, tagApi } from '@/lib/api'
import { BrandCard } from '@/components/cards'
import type { TabacoBrand, Tag } from '@/types'
import { Button } from '@/components/ui/button'
import { AddButton } from '@/components/ui/AddButton'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogClose,
} from '@/components/ui/dialog'
import { toast } from 'sonner'
import { Plus, Search, Package, Tag as TagIcon, X, Hash, Loader2 } from 'lucide-react'

const PAGE_LIMIT = 20

// ─── Brand form dialog ────────────────────────────────────────────────────────

function BrandFormDialog({ brand, isOpen, onClose }: { brand?: TabacoBrand; isOpen: boolean; onClose: () => void }) {
  const qc = useQueryClient()
  const isEdit = !!brand
  const [name, setName]               = useState(brand?.name ?? '')
  const [description, setDescription] = useState(brand?.description ?? '')

  const [synced, setSynced] = useState<TabacoBrand | undefined>(brand)
  if (brand !== synced) { setSynced(brand); setName(brand?.name ?? ''); setDescription(brand?.description ?? '') }

  const createMut = useMutation({
    mutationFn: brandApi.create,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['brands-infinite'] }); onClose(); toast.success('Бренд создан') },
    onError: () => toast.error('Не удалось создать бренд'),
  })
  const updateMut = useMutation({
    mutationFn: ({ id, ...body }: { id: string; name: string; description?: string }) => brandApi.update(id, body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['brands-infinite'] }); onClose(); toast.success('Бренд обновлён') },
    onError: () => toast.error('Не удалось обновить бренд'),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    const body = { name: name.trim(), description: description.trim() || undefined }
    isEdit && brand ? updateMut.mutate({ id: String(brand.id), ...body }) : createMut.mutate(body)
  }

  return (
    <Dialog open={isOpen} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Редактировать бренд' : 'Новый бренд'}</DialogTitle>
          <DialogDescription>{isEdit ? 'Измените данные бренда' : 'Добавьте новый бренд табака'}</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="b-name"><Package className="h-3 w-3 inline mr-1" /> Название</Label>
            <Input id="b-name" value={name} onChange={(e) => setName(e.target.value)} placeholder="Напр. Darkside" required autoFocus />
          </div>
          <div>
            <Label htmlFor="b-desc">Описание (необязательно)</Label>
            <Textarea id="b-desc" value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Краткое описание…" rows={3} />
          </div>
          <div className="flex flex-wrap gap-2 pt-1">
            <Button type="submit" disabled={createMut.isPending || updateMut.isPending || !name.trim()}>
              {createMut.isPending || updateMut.isPending ? 'Сохранение…' : isEdit ? 'Сохранить' : 'Создать бренд'}
            </Button>
            <DialogClose asChild>
              <Button type="button" variant="outline" onClick={onClose}>Отмена</Button>
            </DialogClose>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}

// ─── Tag dropdown component ───────────────────────────────────────────────────

function TagDropdown({
  availableTags,
  existingIds,
  onAdd,
  isAdding,
}: {
  availableTags: Tag[]
  existingIds: Set<string>
  onAdd: (tag: Tag) => void
  isAdding: boolean
}) {
  const [filter, setFilter] = useState('')
  const [open, setOpen]     = useState(false)
  const containerRef        = useRef<HTMLDivElement>(null)

  const filtered = availableTags.filter(
    (t) => !existingIds.has(String(t.id)) && t.name.toLowerCase().includes(filter.toLowerCase()),
  )

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  return (
    // overflow-visible позволяет выпадающему списку выходить за пределы диалога
    <div ref={containerRef} className="relative" style={{ overflow: 'visible' }}>
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
        <input
          className="field pl-9 pr-4"
          placeholder="Выберите или найдите тег…"
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          onFocus={() => setOpen(true)}
          readOnly={isAdding}
        />
      </div>

      {open && (
        <div
          className="absolute left-0 right-0 mt-1 bg-white border border-border rounded-lg shadow-lg overflow-hidden"
          style={{ zIndex: 9999, top: '100%' }}
        >
          {filtered.length === 0 ? (
            <div className="px-4 py-3 text-sm text-ink-muted">
              {availableTags.length === 0 ? 'Теги не загружены' : 'Все теги уже добавлены или не найдены'}
            </div>
          ) : (
            <ul className="max-h-52 overflow-y-auto divide-y divide-border">
              {filtered.map((tag) => (
                <li key={String(tag.id)}>
                  <button
                    type="button"
                    onMouseDown={(e) => {
                      e.preventDefault()
                      onAdd(tag); setFilter(''); setOpen(false)
                    }}
                    disabled={isAdding}
                    className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-left text-ink hover:bg-elevated hover:text-red transition-colors touch-target"
                  >
                    <TagIcon className="h-3.5 w-3.5 text-red-light flex-shrink-0" />
                    {tag.name}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}

// ─── Tag management dialog ────────────────────────────────────────────────────

function BrandTagsDialog({ brand, isOpen, onClose }: { brand: TabacoBrand; isOpen: boolean; onClose: () => void }) {
  const qc = useQueryClient()

  // Local tag list — starts from prop, updated immediately after each mutation
  const [localTags, setLocalTags] = useState(brand.tags)

  // Load all available tags (reuses cache from TagsPage if already visited)
  const { data: tagsData, isLoading: tagsLoading } = useInfiniteQuery({
    queryKey: ['tags-infinite'],
    queryFn: ({ pageParam }) => tagApi.list({ limit: 100, after: pageParam as string | undefined }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (last) => last.nextToken ?? undefined,
  })
  const allTags = tagsData?.pages.flatMap((p) => p.items) ?? []

  const existingTagIds = new Set(localTags.map((t) => String(t.id)))

  const syncCache = (updated: TabacoBrand) => {
    setLocalTags(updated.tags)
    qc.setQueryData(['brands-infinite'], (old: { pages: { items: TabacoBrand[] }[] } | undefined) => {
      if (!old) return old
      return { ...old, pages: old.pages.map((p) => ({ ...p, items: p.items.map((b) => String(b.id) === String(brand.id) ? updated : b) })) }
    })
  }

  const addTagMut = useMutation({
    mutationFn: (tagId: string) => brandApi.addTag({ brandId: String(brand.id), tagId }),
    onSuccess: (updated) => { syncCache(updated); toast.success('Тег добавлен') },
    onError: () => toast.error('Не удалось добавить тег'),
  })

  const removeTagMut = useMutation({
    mutationFn: (tagId: string) => brandApi.removeTag({ brandId: String(brand.id), tagId }),
    onSuccess: (updated) => { syncCache(updated); toast.success('Тег удалён') },
    onError: () => toast.error('Не удалось удалить тег'),
  })

  return (
    <Dialog open={isOpen} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Теги бренда</DialogTitle>
          <DialogDescription>{brand.name}</DialogDescription>
        </DialogHeader>

        {/* Current tags */}
        <div className="mb-4">
          <p className="field-label mb-2">Текущие теги</p>
          {localTags.length === 0 ? (
            <p className="text-sm text-ink-muted italic">Теги не добавлены</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {localTags.map((tag) => (
                <span key={String(tag.id)} className="badge-red flex items-center gap-1.5 pr-1">
                  <TagIcon className="h-3 w-3" />
                  {tag.name}
                  <button
                    onClick={() => removeTagMut.mutate(String(tag.id))}
                    disabled={removeTagMut.isPending}
                    className="ml-0.5 p-0.5 rounded hover:bg-red-glow hover:text-red-dim transition-colors touch-target flex items-center justify-center w-5 h-5"
                    aria-label={`Удалить тег ${tag.name}`}
                  >
                    <X className="h-3 w-3" />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>

        <div className="border-t border-border mb-4" />

        {/* Add via dropdown */}
        <div>
          <p className="field-label mb-2">Добавить тег</p>
          {tagsLoading ? (
            <div className="flex items-center gap-2 text-sm text-ink-muted py-2">
              <Loader2 className="h-4 w-4 animate-spin text-red" /> Загрузка тегов…
            </div>
          ) : (
            <TagDropdown
              availableTags={allTags}
              existingIds={existingTagIds}
              onAdd={(tag) => addTagMut.mutate(String(tag.id))}
              isAdding={addTagMut.isPending}
            />
          )}
          {allTags.length === 0 && !tagsLoading && (
            <p className="text-xs text-ink-muted mt-2">
              Нет доступных тегов.{' '}
              <a href="/admin/tags" className="text-red hover:underline">Создайте теги →</a>
            </p>
          )}
        </div>
      </DialogContent>
    </Dialog>
  )
}

// ─── Delete brand dialog ──────────────────────────────────────────────────────

function DeleteBrandDialog({ brand, isOpen, onClose }: { brand: TabacoBrand; isOpen: boolean; onClose: () => void }) {
  const qc = useQueryClient()
  const deleteMut = useMutation({
    mutationFn: () => brandApi.delete(String(brand.id)),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['brands-infinite'] })
      onClose()
      toast.success('Бренд удалён')
    },
    onError: () => toast.error('Не удалось удалить бренд'),
  })
  return (
    <Dialog open={isOpen} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Удалить бренд?</DialogTitle>
          <DialogDescription>
            Бренд <span className="font-semibold text-ink">{brand.name}</span> будет удалён. Действие необратимо.
          </DialogDescription>
        </DialogHeader>
        <div className="flex gap-2 pt-2">
          <Button variant="danger" disabled={deleteMut.isPending} onClick={() => deleteMut.mutate()}>
            {deleteMut.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : 'Удалить'}
          </Button>
          <DialogClose asChild>
            <Button variant="outline" onClick={onClose}>Отмена</Button>
          </DialogClose>
        </div>
      </DialogContent>
    </Dialog>
  )
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function BrandsPage() {
  const [search, setSearch]           = useState('')
  const [formOpen, setFormOpen]       = useState(false)
  const [editBrand, setEditBrand]     = useState<TabacoBrand | undefined>()
  const [tagsBrand, setTagsBrand]     = useState<TabacoBrand | undefined>()
  const [deleteBrand, setDeleteBrand] = useState<TabacoBrand | undefined>()
  const sentinelRef               = useRef<HTMLDivElement>(null)

  const { data: searchResults, isLoading: searchLoading } = useQuery({
    queryKey: ['brands-search', search],
    queryFn:  () => brandApi.findByName(search.trim()),
    enabled:  search.trim().length >= 2,
    placeholderData: [],
  })

  const { data: infiniteData, isLoading: browseLoading, isFetchingNextPage, fetchNextPage, hasNextPage } = useInfiniteQuery({
    queryKey: ['brands-infinite'],
    queryFn:  ({ pageParam }) => brandApi.list({ limit: PAGE_LIMIT, after: pageParam || undefined }),
    initialPageParam: '',
    getNextPageParam: (last) => last.nextToken || undefined,
    enabled:  search.trim().length < 2,
  })

  const browseBrands  = infiniteData?.pages.flatMap((p) => p.items) ?? []
  const isSearchMode  = search.trim().length >= 2
  const brands        = isSearchMode ? (searchResults ?? []) : browseBrands
  const isLoading     = isSearchMode ? searchLoading : browseLoading
  const totalCount    = isSearchMode ? brands.length : (infiniteData?.pages.reduce((s, p) => s + p.items.length, 0) ?? 0)

  useEffect(() => {
    if (!sentinelRef.current || isSearchMode) return
    const observer = new IntersectionObserver(
      (entries) => { if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) fetchNextPage() },
      { threshold: 0.1 },
    )
    observer.observe(sentinelRef.current)
    return () => observer.disconnect()
  }, [fetchNextPage, hasNextPage, isFetchingNextPage, isSearchMode])

  return (
    <div className="page-root">
      <div className="page-container page-enter">
        {/* Header */}
        <div className="flex flex-wrap items-end justify-between gap-4 mb-8">
          <div>
            <p className="text-xs text-ink-muted font-body uppercase tracking-widest mb-1">Администрирование</p>
            <h1 className="font-display text-3xl sm:text-5xl font-bold text-red">
              Бренды табака
            </h1>
            <div className="red-line w-20 mt-3" />
          </div>
          <AddButton label="Новый бренд" onClick={() => { setEditBrand(undefined); setFormOpen(true) }} />
        </div>

        {/* Search */}
        <div className="relative mb-5">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
          <Input placeholder="Поиск по названию…" value={search} onChange={(e) => setSearch(e.target.value)} className="pl-10" />
        </div>

        {/* Grid */}
        {isLoading ? <BrandsSkeleton /> : brands.length === 0 ? (
          <EmptyState onCreateClick={() => { setEditBrand(undefined); setFormOpen(true) }} />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
            {brands.map((brand) => (
              <BrandCard
                key={String(brand.id)} brand={brand}
                onEdit={() => { setEditBrand(brand); setFormOpen(true) }}
                onManageTags={() => setTagsBrand(brand)}
                onDelete={() => setDeleteBrand(brand)}
              />
            ))}
          </div>
        )}

        {/* Scroll sentinel */}
        {!isSearchMode && (
          <div ref={sentinelRef} className="flex justify-center py-6">
            {isFetchingNextPage && (
              <div className="flex items-center gap-2 text-sm text-ink-muted">
                <Loader2 className="h-4 w-4 animate-spin text-red" /> Загрузка…
              </div>
            )}
            {!hasNextPage && totalCount > 0 && (
              <p className="text-xs text-ink-muted font-body">Все {totalCount} брендов загружены</p>
            )}
          </div>
        )}

        {isSearchMode && !searchLoading && brands.length > 0 && (
          <p className="text-xs text-ink-muted mt-4 text-center font-body">{brands.length} найдено</p>
        )}
      </div>

      <BrandFormDialog brand={editBrand} isOpen={formOpen} onClose={() => setFormOpen(false)} />
      {tagsBrand && (
        <BrandTagsDialog brand={tagsBrand} isOpen={!!tagsBrand} onClose={() => setTagsBrand(undefined)} />
      )}
      {deleteBrand && (
        <DeleteBrandDialog brand={deleteBrand} isOpen={!!deleteBrand} onClose={() => setDeleteBrand(undefined)} />
      )}
    </div>
  )
}

function EmptyState({ onCreateClick }: { onCreateClick: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-16 h-16 rounded-xl bg-red-pale border border-red-glow flex items-center justify-center mb-4">
        <Hash className="h-7 w-7 text-red-light" />
      </div>
      <h3 className="font-display text-xl text-ink mb-2">Брендов пока нет</h3>
      <p className="text-sm text-ink-muted mb-6 max-w-xs">Добавьте первый бренд, чтобы начать каталог</p>
      <Button onClick={onCreateClick}><Plus className="h-4 w-4" /> Создать бренд</Button>
    </div>
  )
}

function BrandsSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 sm:gap-4">
      {[...Array(6)].map((_, i) => (
        <div key={i} className="card p-4 sm:p-5 space-y-3">
          <div className="skeleton h-5 w-2/3" /><div className="skeleton h-4 w-full" />
          <div className="flex gap-2"><div className="skeleton h-5 w-16 rounded-full" /><div className="skeleton h-5 w-12 rounded-full" /></div>
        </div>
      ))}
    </div>
  )
}
