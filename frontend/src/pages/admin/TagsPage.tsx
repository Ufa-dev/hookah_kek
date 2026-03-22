import { useState, useEffect, useRef, type FormEvent } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { tagApi } from '@/lib/api'
import type { Tag } from '@/types'
import { formatDate } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogClose,
} from '@/components/ui/dialog'
import { toast } from 'sonner'
import { Plus, Pencil, Tag as TagIcon, Search, Hash, Check, X, Loader2 } from 'lucide-react'
import { AddButton } from '@/components/ui/AddButton'

const PAGE_LIMIT = 30

// ─── Create tag dialog ────────────────────────────────────────────────────────

function CreateTagDialog({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  const qc = useQueryClient()
  const [name, setName] = useState('')

  const mutation = useMutation({
    mutationFn: tagApi.create,
    onSuccess: (tag) => {
      qc.invalidateQueries({ queryKey: ['tags-infinite'] })
      setName('')
      onClose()
      toast.success(`Тег «${tag.name}» создан`)
    },
    onError: () => toast.error('Не удалось создать тег'),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    if (name.trim().length < 3) return
    mutation.mutate({ name: name.trim() })
  }

  return (
    <Dialog open={isOpen} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Новый тег</DialogTitle>
          <DialogDescription>Тег можно будет назначить брендам</DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="tag-name">
              <TagIcon className="h-3 w-3 inline mr-1" /> Название тега
            </Label>
            <Input
              id="tag-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Напр. Premium, Mild, Fruity…"
              minLength={3}
              maxLength={32}
              required
              autoFocus
            />
            <p className="text-xs text-ink-muted mt-1">3–32 символа</p>
          </div>
          <div className="flex flex-wrap gap-2 pt-1">
            <Button type="submit" disabled={mutation.isPending || name.trim().length < 3}>
              {mutation.isPending ? 'Создание…' : 'Создать тег'}
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

// ─── Inline editable tag row ──────────────────────────────────────────────────

function TagRow({ tag }: { tag: Tag }) {
  const qc = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [name, setName] = useState(tag.name)

  const mutation = useMutation({
    mutationFn: (newName: string) => tagApi.updateName(String(tag.id), { name: newName }),
    onSuccess: (updated) => {
      qc.invalidateQueries({ queryKey: ['tags-infinite'] })
      setEditing(false)
      toast.success(`Переименован в «${updated.name}»`)
    },
    onError: () => {
      toast.error('Не удалось переименовать тег')
      setName(tag.name)
      setEditing(false)
    },
  })

  const handleSave = () => {
    const trimmed = name.trim()
    if (trimmed.length < 3 || trimmed === tag.name) { setEditing(false); setName(tag.name); return }
    mutation.mutate(trimmed)
  }

  return (
    <div className="card flex items-center gap-3 p-3 sm:p-4 group">
      {/* Icon */}
      <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-lg bg-red-pale border border-red-glow flex items-center justify-center flex-shrink-0">
        <TagIcon className="h-3.5 w-3.5 text-red" />
      </div>

      {/* Name / edit */}
      <div className="flex-1 min-w-0">
        {editing ? (
          <div className="flex items-center gap-2">
            <Input
              value={name}
              onChange={(e) => setName(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter') { e.preventDefault(); handleSave() }
                if (e.key === 'Escape') { setEditing(false); setName(tag.name) }
              }}
              className="h-8 text-sm py-1"
              minLength={3}
              maxLength={32}
              autoFocus
            />
            <button
              onClick={handleSave}
              disabled={mutation.isPending || name.trim().length < 3}
              className="p-1.5 rounded hover:bg-red-pale text-red transition-colors touch-target flex items-center justify-center flex-shrink-0"
              aria-label="Сохранить"
            >
              <Check className="h-4 w-4" />
            </button>
            <button
              onClick={() => { setEditing(false); setName(tag.name) }}
              className="p-1.5 rounded hover:bg-hover text-ink-muted transition-colors touch-target flex items-center justify-center flex-shrink-0"
              aria-label="Отмена"
            >
              <X className="h-4 w-4" />
            </button>
          </div>
        ) : (
          <p className="font-body font-medium text-ink truncate">{tag.name}</p>
        )}
        <p className="text-xs text-ink-muted mt-0.5">
          {formatDate(typeof tag.updatedAt === 'string' ? tag.updatedAt : tag.createdAt)}
        </p>
      </div>

      {/* Edit button */}
      {!editing && (
        <button
          onClick={() => setEditing(true)}
          className="opacity-0 group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-red-pale text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center flex-shrink-0"
          aria-label={`Переименовать тег ${tag.name}`}
        >
          <Pencil className="h-3.5 w-3.5" />
        </button>
      )}
    </div>
  )
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function TagsPage() {
  const [search, setSearch]     = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [exactResult, setExactResult] = useState<Tag | null | 'not-found' | 'idle'>('idle')
  const [exactSearching, setExactSearching] = useState(false)
  const sentinelRef = useRef<HTMLDivElement>(null)

  // ── Infinite cursor scroll ──────────────────────────────────────────
  const {
    data,
    isLoading,
    isFetchingNextPage,
    fetchNextPage,
    hasNextPage,
  } = useInfiniteQuery({
    queryKey: ['tags-infinite'],
    queryFn: ({ pageParam }) =>
      tagApi.list({ limit: PAGE_LIMIT, after: pageParam as string | undefined }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (lastPage) => lastPage.nextToken ?? undefined,
  })

  const allTags = data?.pages.flatMap((p) => p.items) ?? []
  const totalLoaded = allTags.length

  // Client-side filter for already-loaded items
  const filtered = search.trim()
    ? allTags.filter((t) => t.name.toLowerCase().includes(search.toLowerCase()))
    : allTags

  // Intersection observer for auto-load
  useEffect(() => {
    if (!sentinelRef.current) return
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage()
        }
      },
      { threshold: 0.1 },
    )
    observer.observe(sentinelRef.current)
    return () => observer.disconnect()
  }, [fetchNextPage, hasNextPage, isFetchingNextPage])

  // Exact name search via API (finds tags not yet loaded in current page set)
  const handleExactSearch = async () => {
    if (!search.trim()) return
    setExactSearching(true)
    try {
      const tag = await tagApi.findByName(search.trim())
      setExactResult(tag)
    } catch {
      setExactResult('not-found')
    } finally {
      setExactSearching(false)
    }
  }

  return (
    <div className="page-root">
      <div className="page-container page-enter">

        {/* Header */}
        <div className="flex flex-wrap items-end justify-between gap-4 mb-8">
          <div>
            <p className="text-xs text-ink-muted font-body uppercase tracking-widest mb-1">Администрирование</p>
            <h1 className="font-display text-3xl sm:text-5xl text-ink">
              Теги <span className="text-red">брендов</span>
            </h1>
            <div className="red-line w-20 mt-3" />
          </div>
          <AddButton label="Новый тег" onClick={() => setCreateOpen(true)} />
        </div>

        {/* Search bar */}
        <div className="flex gap-2 mb-5">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-ink-muted pointer-events-none" />
            <Input
              placeholder="Фильтр по загруженным или точный поиск…"
              value={search}
              onChange={(e) => { setSearch(e.target.value); setExactResult('idle') }}
              onKeyDown={(e) => e.key === 'Enter' && handleExactSearch()}
              className="pl-10"
            />
          </div>
          <Button
            type="button"
            variant="outline"
            onClick={handleExactSearch}
            disabled={exactSearching || !search.trim()}
            className="flex-shrink-0"
          >
            {exactSearching
              ? <Loader2 className="h-4 w-4 animate-spin" />
              : <Search className="h-4 w-4" />}
            <span className="hidden sm:inline">Найти</span>
          </Button>
        </div>

        {/* Exact search result banner */}
        {exactResult !== 'idle' && (
          <div className="mb-4">
            {exactResult === 'not-found' ? (
              <div className="flex items-center gap-2 p-3 bg-elevated border border-border rounded text-sm text-ink-muted">
                <X className="h-4 w-4 text-red-500 flex-shrink-0" />
                Тег «{search}» не найден. Создайте его кнопкой «Новый тег».
              </div>
            ) : (
              <div className="p-3 bg-red-pale border border-red-glow rounded text-sm text-red-dim">
                <TagIcon className="h-4 w-4 inline mr-1.5" />
                Найден: <strong>{exactResult.name}</strong>
              </div>
            )}
          </div>
        )}

        {/* Count */}
        {!isLoading && (
          <p className="text-xs text-ink-muted mb-3 font-body">
            {search.trim() ? `${filtered.length} из ${totalLoaded} загруженных` : `${totalLoaded} тегов загружено`}
          </p>
        )}

        {/* Tag list */}
        {isLoading ? (
          <TagsSkeleton />
        ) : filtered.length === 0 ? (
          <EmptyTags onCreateClick={() => setCreateOpen(true)} />
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3">
            {filtered.map((tag) => (
              <TagRow key={String(tag.id)} tag={tag} />
            ))}
          </div>
        )}

        {/* Infinite scroll sentinel */}
        {!search.trim() && (
          <div ref={sentinelRef} className="flex justify-center py-6">
            {isFetchingNextPage && (
              <div className="flex items-center gap-2 text-sm text-ink-muted">
                <Loader2 className="h-4 w-4 animate-spin text-red" />
                Загрузка…
              </div>
            )}
            {!hasNextPage && totalLoaded > 0 && (
              <p className="text-xs text-ink-muted font-body">Все {totalLoaded} тегов загружены</p>
            )}
          </div>
        )}
      </div>

      <CreateTagDialog isOpen={createOpen} onClose={() => setCreateOpen(false)} />
    </div>
  )
}

function EmptyTags({ onCreateClick }: { onCreateClick: () => void }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-16 h-16 rounded-xl bg-surface border border-border flex items-center justify-center mb-4">
        <Hash className="h-7 w-7 text-ink-muted" />
      </div>
      <h3 className="font-display text-xl text-ink mb-2">Тегов пока нет</h3>
      <p className="text-sm text-ink-muted mb-6 max-w-xs">
        Создайте теги и назначьте их брендам для удобной фильтрации
      </p>
      <Button onClick={onCreateClick}><Plus className="h-4 w-4" /> Создать тег</Button>
    </div>
  )
}

function TagsSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 sm:gap-3">
      {[...Array(8)].map((_, i) => (
        <div key={i} className="card p-3 sm:p-4 flex items-center gap-3">
          <div className="skeleton w-9 h-9 rounded-lg flex-shrink-0" />
          <div className="flex-1 space-y-2">
            <div className="skeleton h-4 w-24 rounded" />
            <div className="skeleton h-3 w-16 rounded" />
          </div>
        </div>
      ))}
    </div>
  )
}
