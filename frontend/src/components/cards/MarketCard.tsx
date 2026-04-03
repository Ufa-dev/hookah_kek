import { Pencil, Clock, Trash2, Minus, Plus } from 'lucide-react'
import type { MarketArcView } from '@/types'
import { Badge } from '@/components/ui/badge'
import { formatDate } from '@/lib/utils'

interface MarketCardProps {
  item: MarketArcView
  onEdit: () => void
  onDelete: () => void
  onCountChange: (newCount: number) => void
}

export function MarketCard({ item, onEdit, onDelete, onCountChange }: MarketCardProps) {
  return (
    <div className="card group flex flex-col hover:border-red-light transition-colors duration-150">
      <div className="p-4 sm:p-5 flex-1">
        <div className="flex items-start justify-between gap-2 mb-3">
          <h3 className="font-display text-base text-ink leading-snug flex-1">{item.name}</h3>
          <button
            onClick={onEdit}
            className="opacity-0 group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-elevated text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center flex-shrink-0"
            aria-label="Редактировать"
          >
            <Pencil className="h-3.5 w-3.5" />
          </button>
        </div>
        <div className="flex flex-wrap gap-1.5">
          <Badge variant="surface">{item.brandName}</Badge>
          <Badge variant="outline">{item.flavorName}</Badge>
          <Badge variant="outline">{item.weightGrams} г</Badge>
          {item.gtin && (
            <Badge variant="outline">
              <span className="font-mono">{item.gtin}</span>
            </Badge>
          )}
        </div>
      </div>
      <div className="border-t border-border px-4 sm:px-5 py-3 flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 text-xs text-ink-muted min-w-0">
          <Clock className="h-3 w-3 flex-shrink-0" />
          <span className="truncate">{formatDate(item.updatedAt)}</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1">
            <button
              onClick={() => onCountChange(Math.max(0, item.count - 1))}
              disabled={item.count === 0}
              className="p-1 rounded hover:bg-elevated text-ink-muted hover:text-red transition-colors disabled:opacity-30 disabled:cursor-not-allowed touch-target flex items-center justify-center"
              aria-label="Уменьшить количество"
            >
              <Minus className="h-3 w-3" />
            </button>
            <span className="text-xs font-body font-semibold text-ink min-w-[1.5rem] text-center">
              {item.count}
            </span>
            <button
              onClick={() => onCountChange(item.count + 1)}
              className="p-1 rounded hover:bg-elevated text-ink-muted hover:text-red transition-colors touch-target flex items-center justify-center"
              aria-label="Увеличить количество"
            >
              <Plus className="h-3 w-3" />
            </button>
          </div>
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
  )
}
