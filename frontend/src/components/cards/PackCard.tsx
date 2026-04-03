import { useState } from 'react'
import { Pencil, Clock, Trash2, ChevronDown, ChevronUp } from 'lucide-react'
import type { FlavorPack } from '@/types'
import { formatDate } from '@/lib/utils'
import { WeightBar } from '@/components/cards/WeightBar'

interface PackCardProps {
  pack: FlavorPack
  warehouseWeightGrams?: number
  onEdit: () => void
  onDelete: () => void
}

export function PackCard({ pack, warehouseWeightGrams, onEdit, onDelete }: PackCardProps) {
  const [expanded, setExpanded] = useState(false)

  return (
    <div className="card group flex flex-col hover:border-red-light transition-colors duration-150">
      <div className="p-4 sm:p-5 flex-1">
        <div className="mb-3">
          <div className="flex items-center gap-1 min-w-0">
            <p className={`text-sm font-semibold text-ink flex-1 min-w-0 ${expanded ? 'break-words' : 'truncate'}`}>
              {pack.name}
            </p>
            <button
              onClick={() => setExpanded(e => !e)}
              className="flex-shrink-0 p-0.5 rounded text-ink-muted hover:text-ink transition-colors"
              aria-label={expanded ? 'Свернуть' : 'Развернуть'}
            >
              {expanded ? <ChevronUp className="h-3 w-3" /> : <ChevronDown className="h-3 w-3" />}
            </button>
          </div>
          <p className="font-mono text-xs text-ink-muted mt-0.5 truncate">{pack.id}</p>
          {pack.flavorId && (
            <p className="text-xs text-ink-muted mt-0.5 truncate">
              Вкус: <span className="font-mono">{pack.flavorId.slice(0, 8)}…</span>
            </p>
          )}
        </div>
        <WeightBar current={pack.currentWeightGrams} total={pack.totalWeightGrams} />
        {warehouseWeightGrams !== undefined && (
          <div className="mt-2 text-xs text-ink-dim flex items-center gap-1">
            <span>Склад:</span>
            <span className="font-semibold text-gold">{warehouseWeightGrams} г</span>
            <span className="text-ink-muted">→ итого</span>
            <span className="font-semibold text-ink">{pack.currentWeightGrams + warehouseWeightGrams} г</span>
          </div>
        )}
      </div>
      <div className="border-t border-border px-4 sm:px-5 py-3 flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 text-xs text-ink-muted min-w-0">
          <Clock className="h-3 w-3 flex-shrink-0" />
          <span className="truncate">{formatDate(pack.updatedAt)}</span>
        </div>
        <div className="flex items-center gap-1 flex-shrink-0">
          <button
            onClick={onEdit}
            className="sm:opacity-0 sm:group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-elevated text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center"
            aria-label="Редактировать"
          >
            <Pencil className="h-3.5 w-3.5" />
          </button>
          <button
            onClick={onDelete}
            className="sm:opacity-0 sm:group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-elevated text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center"
            aria-label="Удалить"
          >
            <Trash2 className="h-3.5 w-3.5" />
          </button>
        </div>
      </div>
    </div>
  )
}
