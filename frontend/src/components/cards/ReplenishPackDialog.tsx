import { useEffect, useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { marketApi, packApi } from '@/lib/api'
import type { FlavorPack, MarketArcView } from '@/types'
import {
  Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogClose,
} from '@/components/ui/dialog'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Loader2 } from 'lucide-react'
import { toast } from 'sonner'

type Step = 'pre-weigh' | 'select-item' | 'confirm' | 'post-weigh'

export function ReplenishPackDialog({
  pack,
  isOpen,
  onClose,
}: {
  pack: FlavorPack
  isOpen: boolean
  onClose: () => void
}) {
  const qc = useQueryClient()
  const [step, setStep] = useState<Step>('pre-weigh')
  const [preWeighGrams, setPreWeighGrams] = useState(String(pack.currentWeightGrams))
  const [selectedItem, setSelectedItem] = useState<MarketArcView | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [postWeighGrams, setPostWeighGrams] = useState('')

  useEffect(() => {
    if (!isOpen) {
      setStep('pre-weigh')
      setSelectedItem(null)
      setQuantity(1)
      setPostWeighGrams('')
    }
  }, [isOpen])

  useEffect(() => {
    if (isOpen) {
      setPreWeighGrams(String(pack.currentWeightGrams))
    }
  }, [isOpen, pack.currentWeightGrams])

  const { data: marketItems, isLoading: marketLoading } = useQuery({
    queryKey: ['market-by-flavor', pack.flavorId],
    queryFn: () => marketApi.list({ flavorId: pack.flavorId!, countMin: 1, limit: 100 }).then(r => r.data),
    enabled: isOpen && step === 'select-item' && !!pack.flavorId,
  })

  const deductMut = useMutation({
    mutationFn: () => marketApi.updateCount(selectedItem!.id, { count: selectedItem!.count - quantity }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['market-by-flavor', pack.flavorId] })
      setStep('post-weigh')
    },
    onError: () => toast.error('Не удалось списать со склада'),
  })

  const updatePackMut = useMutation({
    mutationFn: (grams: number) =>
      packApi.update(pack.id, {
        name: pack.name,
        flavorId: pack.flavorId ?? undefined,
        currentWeightGrams: grams,
        totalWeightGrams: grams,
      }),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['packs-infinite'] })
      toast.success('Контейнер обновлён')
      onClose()
    },
    onError: () => toast.error('Не удалось обновить контейнер'),
  })

  const items: MarketArcView[] = marketItems ?? []

  return (
    <Dialog open={isOpen} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        {step === 'pre-weigh' && (
          <>
            <DialogHeader>
              <DialogTitle>Пополнить контейнер</DialogTitle>
              <DialogDescription>Укажите текущий вес контарки</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="pre-weigh-input">Текущий вес (г)</Label>
                <Input
                  id="pre-weigh-input"
                  type="number"
                  min={0}
                  value={preWeighGrams}
                  onChange={e => setPreWeighGrams(e.target.value)}
                  autoFocus
                />
              </div>
              <div className="flex gap-2">
                <Button onClick={() => setStep('select-item')} disabled={preWeighGrams === ''}>
                  Далее
                </Button>
                <DialogClose asChild>
                  <Button variant="outline" onClick={onClose}>Закрыть</Button>
                </DialogClose>
              </div>
            </div>
          </>
        )}

        {step === 'select-item' && (
          <>
            <DialogHeader>
              <DialogTitle>Выберите товар со склада</DialogTitle>
              <DialogDescription>Выберите SKU для списания</DialogDescription>
            </DialogHeader>
            {!pack.flavorId ? (
              <div className="space-y-4">
                <p className="text-sm text-ink-muted">У этого контейнера не указан вкус</p>
                <DialogClose asChild>
                  <Button variant="outline" onClick={onClose}>Закрыть</Button>
                </DialogClose>
              </div>
            ) : marketLoading ? (
              <div className="flex items-center gap-2 py-4 text-ink-muted">
                <Loader2 className="h-4 w-4 animate-spin" /> Загрузка…
              </div>
            ) : items.length === 0 ? (
              <div className="space-y-4">
                <p className="text-sm text-ink-muted">Табака с таким вкусом на складе нет</p>
                <DialogClose asChild>
                  <Button variant="outline" onClick={onClose}>Закрыть</Button>
                </DialogClose>
              </div>
            ) : (
              <div className="space-y-4">
                <div className="space-y-2 max-h-60 overflow-y-auto">
                  {items.map(item => (
                    <label
                      key={item.id}
                      className={`flex items-start gap-3 p-3 rounded-lg border cursor-pointer transition-colors ${
                        selectedItem?.id === item.id
                          ? 'border-gold bg-gold/5'
                          : 'border-border hover:border-gold/50'
                      }`}
                    >
                      <input
                        type="radio"
                        name="market-item"
                        className="mt-0.5 accent-gold"
                        checked={selectedItem?.id === item.id}
                        onChange={() => { setSelectedItem(item); setQuantity(1) }}
                      />
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-ink truncate">{item.name}</p>
                        <p className="text-xs text-ink-muted">{item.weightGrams} г · {item.count} шт на складе</p>
                      </div>
                    </label>
                  ))}
                </div>
                {selectedItem && (
                  <div>
                    <Label htmlFor="qty-input">Количество (шт)</Label>
                    <Input
                      id="qty-input"
                      type="number"
                      min={1}
                      max={selectedItem.count}
                      value={quantity}
                      onChange={e => setQuantity(Math.max(1, Math.min(selectedItem.count, parseInt(e.target.value) || 1)))}
                    />
                  </div>
                )}
                <div className="flex gap-2">
                  <Button
                    onClick={() => setStep('confirm')}
                    disabled={!selectedItem || quantity < 1}
                  >
                    Перенести в контейнер
                  </Button>
                  <Button variant="outline" onClick={() => setStep('pre-weigh')}>Назад</Button>
                </div>
              </div>
            )}
          </>
        )}

        {step === 'confirm' && selectedItem && (
          <>
            <DialogHeader>
              <DialogTitle>Подтверждение списания</DialogTitle>
              <DialogDescription>
                Подтвердите списание со склада «{selectedItem.name}» в количестве {quantity} шт
              </DialogDescription>
            </DialogHeader>
            <div className="flex gap-2">
              <Button onClick={() => deductMut.mutate()} disabled={deductMut.isPending}>
                {deductMut.isPending
                  ? <><Loader2 className="h-4 w-4 mr-2 animate-spin" />Списание…</>
                  : 'Подтвердить'}
              </Button>
              <Button variant="outline" onClick={() => setStep('select-item')} disabled={deductMut.isPending}>
                Назад
              </Button>
            </div>
          </>
        )}

        {step === 'post-weigh' && (
          <>
            <DialogHeader>
              <DialogTitle>Взвесьте контейнер</DialogTitle>
              <DialogDescription>Взвесьте повторно контейнер после пополнения</DialogDescription>
            </DialogHeader>
            <div className="space-y-4">
              <div>
                <Label htmlFor="post-weigh-input">Новый вес (г)</Label>
                <Input
                  id="post-weigh-input"
                  type="number"
                  min={0}
                  value={postWeighGrams}
                  onChange={e => setPostWeighGrams(e.target.value)}
                  autoFocus
                />
              </div>
              <div className="flex gap-2">
                <Button
                  onClick={() => updatePackMut.mutate(parseInt(postWeighGrams, 10))}
                  disabled={updatePackMut.isPending || postWeighGrams === '' || isNaN(parseInt(postWeighGrams, 10))}
                >
                  {updatePackMut.isPending
                    ? <><Loader2 className="h-4 w-4 mr-2 animate-spin" />Сохранение…</>
                    : 'Сохранить'}
                </Button>
                <DialogClose asChild>
                  <Button variant="outline" onClick={onClose}>Закрыть</Button>
                </DialogClose>
              </div>
            </div>
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}
