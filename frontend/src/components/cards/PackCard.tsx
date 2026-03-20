import { Pencil, Clock, Trash2 } from 'lucide-react'
import type { FlavorPack } from '@/types'
import { formatDate } from '@/lib/utils'

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

interface PackCardProps {
  pack: FlavorPack
  onEdit: () => void
  onDelete: () => void
}

export function PackCard({ pack, onEdit, onDelete }: PackCardProps) {
  return (
    <div className="card group flex flex-col hover:border-red-light transition-colors duration-150">
      <div className="p-4 sm:p-5 flex-1">
        <div className="flex items-start justify-between gap-2 mb-3">
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold text-ink truncate">{pack.name}</p>
            <p className="font-mono text-xs text-ink-muted mt-0.5 truncate">{pack.id}</p>
            {pack.flavorId && (
              <p className="text-xs text-ink-muted mt-0.5 truncate">
                Вкус: <span className="font-mono">{pack.flavorId.slice(0, 8)}…</span>
              </p>
            )}
          </div>
          <button
            onClick={onEdit}
            className="opacity-0 group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-elevated text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center flex-shrink-0"
            aria-label="Редактировать"
          >
            <Pencil className="h-3.5 w-3.5" />
          </button>
        </div>
        <WeightBar current={pack.currentWeightGrams} total={pack.totalWeightGrams} />
      </div>
      <div className="border-t border-border px-4 sm:px-5 py-3 flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 text-xs text-ink-muted min-w-0">
          <Clock className="h-3 w-3 flex-shrink-0" />
          <span className="truncate">{formatDate(pack.updatedAt)}</span>
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
  )
}
