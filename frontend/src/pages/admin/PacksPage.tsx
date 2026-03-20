import { useState, type FormEvent } from 'react'
import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { packApi } from '@/lib/api'
import type { FlavorPack } from '@/types'
import { formatDate } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogClose,
} from '@/components/ui/dialog'
import { toast } from 'sonner'
import { Plus, Pencil, Trash2, Archive, Loader2, Weight } from 'lucide-react'

const PAGE_LIMIT = 20

// ─── Weight progress bar ──────────────────────────────────────────────────────

function WeightBar({ current, total }: { current: number; total: number }) {
  const pct = total > 0 ? Math.round((current / total) * 100) : 0
  const color =
    pct > 50 ? 'bg-green-500' :
    pct > 20 ? 'bg-yellow-500' :
               'bg-red'

  return (
    <div className="space-y-1">
      <div className="flex justify-between text-xs text-ink-muted">
        <span>{current} г</span>
        <span>{pct}%</span>
        <span>{total} г</span>
      </div>
      <div className="h-2 bg-elevated rounded-full overflow-hidden">
        <div className={`h-full ${color} transition-all duration-300`} style={{ width: `${pct}%` }} />
      </div>
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
            <Label htmlFor="p-flavor">UUID вкуса (необязательно)</Label>
            <Input
              id="p-flavor"
              value={flavorId}
              onChange={(e) => setFlavorId(e.target.value)}
              placeholder="xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
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
          <Button variant="destructive" disabled={deleteMut.isPending} onClick={() => deleteMut.mutate()}>
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

// ─── Pack card ────────────────────────────────────────────────────────────────

function PackCard({
  pack,
  onEdit,
  onDelete,
}: {
  pack: FlavorPack
  onEdit: (p: FlavorPack) => void
  onDelete: (p: FlavorPack) => void
}) {
  return (
    <div className="card group relative">
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0 flex-1">
          <p className="text-sm font-semibold text-ink truncate">{pack.name}</p>
          <p className="font-mono text-xs text-ink-muted mt-0.5 truncate">{pack.id}</p>
          {pack.flavorId && (
            <p className="text-xs text-ink-muted mt-0.5 truncate">
              Вкус: <span className="font-mono">{pack.flavorId}</span>
            </p>
          )}
        </div>
        <div className="flex items-center gap-1 flex-shrink-0">
          <button
            onClick={() => onEdit(pack)}
            className="touch-target rounded-lg p-1.5 text-ink-muted hover:text-ink hover:bg-elevated transition-colors opacity-0 group-hover:opacity-100"
            title="Редактировать"
          >
            <Pencil className="h-3.5 w-3.5" />
          </button>
          <button
            onClick={() => onDelete(pack)}
            className="touch-target rounded-lg p-1.5 text-ink-muted hover:text-red transition-colors opacity-0 group-hover:opacity-100"
            title="Удалить"
          >
            <Trash2 className="h-3.5 w-3.5" />
          </button>
        </div>
      </div>

      <div className="mt-3">
        <WeightBar current={pack.currentWeightGrams} total={pack.totalWeightGrams} />
      </div>

      <p className="text-xs text-ink-muted mt-2">
        Обновлён {formatDate(pack.updatedAt)}
      </p>
    </div>
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
                  onEdit={openEdit}
                  onDelete={(p) => setDeletePack(p)}
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
