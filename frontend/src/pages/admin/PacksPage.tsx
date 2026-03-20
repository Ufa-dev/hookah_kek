import { useState, useRef, useEffect, type FormEvent } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { packApi, flavorApi } from '@/lib/api'
import { PackCard } from '@/components/cards'
import type { FlavorPack, TabacoFlavor } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogClose,
} from '@/components/ui/dialog'
import { toast } from 'sonner'
import { Plus, Archive, Loader2, Weight, X, ChevronDown } from 'lucide-react'

const PAGE_LIMIT = 20

// ─── Weight preview bar (used in PackFormDialog) ──────────────────────────────

function WeightBar({ current, total }: { current: number; total: number }) {
  const pct = total > 0 ? Math.round((current / total) * 100) : 0
  const color = pct > 50 ? 'bg-green-500' : pct > 20 ? 'bg-yellow-500' : 'bg-red'
  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs text-ink-muted">
        <span>{current} г</span><span>{pct}%</span><span>{total} г</span>
      </div>
      <div className="h-2 bg-elevated rounded-full overflow-hidden">
        <div className={`h-full ${color} transition-all duration-300`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}

// ─── Flavor selector ──────────────────────────────────────────────────────────

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

// ─── Pack form dialog ─────────────────────────────────────────────────────────

function PackFormDialog({
  pack,
  isOpen,
  onClose,
}: {
  pack?: FlavorPack
  isOpen: boolean
  onClose: () => void
}) {
  const qc = useQueryClient()
  const isEdit = !!pack

  const [id, setId]             = useState(pack?.id ?? '')
  const [name, setName]         = useState(pack?.name ?? '')
  const [flavorId, setFlavorId] = useState(pack?.flavorId ?? '')
  const [current, setCurrent]   = useState(String(pack?.currentWeightGrams ?? ''))
  const [total, setTotal]       = useState(String(pack?.totalWeightGrams ?? ''))

  const [synced, setSynced] = useState<FlavorPack | undefined>(pack)
  if (pack !== synced) {
    setSynced(pack)
    setId(pack?.id ?? '')
    setName(pack?.name ?? '')
    setFlavorId(pack?.flavorId ?? '')
    setCurrent(String(pack?.currentWeightGrams ?? ''))
    setTotal(String(pack?.totalWeightGrams ?? ''))
  }

  const invalidate = () => qc.invalidateQueries({ queryKey: ['packs-infinite'] })

  const createMut = useMutation({
    mutationFn: packApi.create,
    onSuccess: () => { invalidate(); onClose(); toast.success('Контейнер создан') },
    onError: () => toast.error('Не удалось создать контейнер'),
  })

  const updateMut = useMutation({
    mutationFn: ({ packId, ...body }: { packId: string; flavorId?: string; currentWeightGrams: number; totalWeightGrams: number }) =>
      packApi.update(packId, body),
    onSuccess: () => { invalidate(); onClose(); toast.success('Контейнер обновлён') },
    onError: () => toast.error('Не удалось обновить контейнер'),
  })

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault()
    const cur = parseInt(current, 10)
    const tot = parseInt(total, 10)
    if (!id.trim() || isNaN(cur) || isNaN(tot)) return
    if (tot <= 0) { toast.error('Общий вес должен быть больше 0'); return }
    if (cur < 0) { toast.error('Текущий вес не может быть отрицательным'); return }
    if (cur > tot) { toast.error('Текущий вес не может превышать общий'); return }

    const body = {
      name: name.trim(),
      flavorId: flavorId.trim() || undefined,
      currentWeightGrams: cur,
      totalWeightGrams: tot,
    }

    if (isEdit && pack) {
      updateMut.mutate({ packId: pack.id, ...body })
    } else {
      createMut.mutate({ id: id.trim(), ...body })
    }
  }

  const isPending = createMut.isPending || updateMut.isPending

  return (
    <Dialog open={isOpen} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{isEdit ? 'Редактировать контейнер' : 'Новый контейнер'}</DialogTitle>
          <DialogDescription>
            {isEdit ? 'Измените данные контейнера' : 'Добавьте новый контейнер табака'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="p-id">Идентификатор</Label>
            <Input
              id="p-id"
              value={id}
              onChange={(e) => setId(e.target.value)}
              placeholder="Напр. darkside-two-apples-250g-1"
              disabled={isEdit}
              required
              autoFocus={!isEdit}
            />
          </div>
          <div>
            <Label htmlFor="p-name">Название</Label>
            <Input
              id="p-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Напр. Darkside Two Apples 250г"
              required
              autoFocus={isEdit}
            />
          </div>
          <div>
            <Label>Вкус (необязательно)</Label>
            <FlavorSelector
              value={flavorId}
              onChange={(id) => setFlavorId(id)}
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <Label htmlFor="p-total">Общий вес (г)</Label>
              <Input
                id="p-total"
                type="number"
                min={1}
                value={total}
                onChange={(e) => setTotal(e.target.value)}
                placeholder="100"
                required
              />
            </div>
            <div>
              <Label htmlFor="p-current">Текущий вес (г)</Label>
              <Input
                id="p-current"
                type="number"
                min={0}
                value={current}
                onChange={(e) => setCurrent(e.target.value)}
                placeholder="50"
                required
              />
            </div>
          </div>
          {!isNaN(parseInt(current)) && !isNaN(parseInt(total)) && parseInt(total) > 0 && (
            <WeightBar current={parseInt(current)} total={parseInt(total)} />
          )}
          <div className="flex flex-wrap gap-2 pt-1">
            <Button
              type="submit"
              disabled={isPending || !id.trim() || !name.trim() || !current || !total}
            >
              {isPending
                ? <><Loader2 className="h-4 w-4 mr-2 animate-spin" />Сохранение…</>
                : isEdit ? 'Сохранить' : 'Создать'}
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

// ─── Delete confirmation dialog ────────────────────────────────────────────────

function DeletePackDialog({
  pack,
  isOpen,
  onClose,
}: {
  pack: FlavorPack
  isOpen: boolean
  onClose: () => void
}) {
  const qc = useQueryClient()

  const deleteMut = useMutation({
    mutationFn: () => packApi.delete(pack.id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['packs-infinite'] })
      onClose()
      toast.success('Контейнер удалён')
    },
    onError: () => toast.error('Не удалось удалить контейнер'),
  })

  return (
    <Dialog open={isOpen} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Удалить контейнер?</DialogTitle>
          <DialogDescription>
            Контейнер <span className="font-mono text-ink">{pack.id}</span> будет удалён. Действие необратимо.
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

// ─── Main page ────────────────────────────────────────────────────────────────

export default function PacksPage() {
  const qc = useQueryClient()
  const [formOpen, setFormOpen]     = useState(false)
  const [editPack, setEditPack]     = useState<FlavorPack | undefined>()
  const [deletePack, setDeletePack] = useState<FlavorPack | undefined>()

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useInfiniteQuery({
    queryKey: ['packs-infinite'],
    queryFn: ({ pageParam }) =>
      packApi.list({ limit: PAGE_LIMIT, after: pageParam as string | undefined }),
    initialPageParam: undefined as string | undefined,
    getNextPageParam: (last) => last.nextToken ?? undefined,
  })

  const packs = data?.pages.flatMap((p) => p.items) ?? []

  const openCreate = () => { setEditPack(undefined); setFormOpen(true) }
  const openEdit   = (p: FlavorPack) => { setEditPack(p); setFormOpen(true) }

  return (
    <div className="page-root">
      <div className="page-container page-enter">

        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-red-pale border border-red-glow flex items-center justify-center">
              <Archive className="h-4.5 w-4.5 text-red" />
            </div>
            <div>
              <h1 className="font-display text-xl text-ink">Контейнеры</h1>
              <p className="text-xs text-ink-muted">Учёт табака по весу</p>
            </div>
          </div>
          <Button onClick={openCreate} size="sm" className="gap-1.5">
            <Plus className="h-4 w-4" /> Добавить
          </Button>
        </div>

        {/* Pack list */}
        {isLoading ? (
          <div className="flex items-center justify-center py-16 text-ink-muted">
            <Loader2 className="h-5 w-5 animate-spin mr-2" /> Загрузка…
          </div>
        ) : packs.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center gap-3">
            <Weight className="h-10 w-10 text-ink-muted opacity-40" />
            <p className="text-ink-muted">Контейнеры не найдены</p>
            <Button variant="outline" size="sm" onClick={openCreate}>
              <Plus className="h-4 w-4 mr-1.5" /> Создать первый
            </Button>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
              {packs.map((pack) => (
                <PackCard
                  key={pack.id}
                  pack={pack}
                  onEdit={() => openEdit(pack)}
                  onDelete={() => setDeletePack(pack)}
                />
              ))}
            </div>

            {hasNextPage && (
              <div className="flex justify-center mt-6">
                <Button
                  variant="outline"
                  onClick={() => fetchNextPage()}
                  disabled={isFetchingNextPage}
                >
                  {isFetchingNextPage
                    ? <><Loader2 className="h-4 w-4 mr-2 animate-spin" />Загрузка…</>
                    : 'Загрузить ещё'}
                </Button>
              </div>
            )}
          </>
        )}
      </div>

      {/* Dialogs */}
      <PackFormDialog
        pack={editPack}
        isOpen={formOpen}
        onClose={() => setFormOpen(false)}
      />
      {deletePack && (
        <DeletePackDialog
          pack={deletePack}
          isOpen={!!deletePack}
          onClose={() => setDeletePack(undefined)}
        />
      )}
    </div>
  )
}
