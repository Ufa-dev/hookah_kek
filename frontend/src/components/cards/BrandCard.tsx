import { Pencil, Clock, Tag as TagIcon, Trash2 } from 'lucide-react'
import type { TabacoBrand } from '@/types'
import { Badge } from '@/components/ui/badge'
import { formatDate } from '@/lib/utils'

interface BrandCardProps {
  brand: TabacoBrand
  onEdit: () => void
  onManageTags: () => void
  onDelete: () => void
}

export function BrandCard({ brand, onEdit, onManageTags, onDelete }: BrandCardProps) {
  return (
    <div className="card group flex flex-col hover:border-red-light transition-colors duration-150">
      <div className="p-4 sm:p-5 flex-1">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="font-display text-lg sm:text-xl text-ink leading-snug">{brand.name}</h3>
          <button
            onClick={onEdit}
            className="opacity-0 group-hover:opacity-100 focus:opacity-100 p-1.5 rounded hover:bg-elevated text-ink-muted hover:text-red transition-all touch-target flex items-center justify-center"
            aria-label="Редактировать"
          >
            <Pencil className="h-3.5 w-3.5" />
          </button>
        </div>
        {brand.description && <p className="text-sm text-ink-dim mb-3 line-clamp-2">{brand.description}</p>}
        <div className="flex flex-wrap gap-1.5 mb-3 min-h-[26px]">
          {brand.tags.length > 0
            ? brand.tags.map((tag) => (
                <Badge key={String(tag.id)}>
                  <TagIcon className="h-2.5 w-2.5" />{tag.name}
                </Badge>
              ))
            : <span className="text-xs text-ink-muted italic">Нет тегов</span>
          }
        </div>
      </div>
      <div className="border-t border-border px-4 sm:px-5 py-3 flex items-center justify-between gap-2">
        <div className="flex items-center gap-1.5 text-xs text-ink-muted min-w-0">
          <Clock className="h-3 w-3 flex-shrink-0" />
          <span className="truncate">{formatDate(brand.updatedAt)}</span>
        </div>
        <div className="flex items-center gap-2 flex-shrink-0">
          <button onClick={onManageTags} className="flex items-center gap-1 text-xs font-body font-medium text-red hover:text-red-dim transition-colors touch-target">
            <TagIcon className="h-3.5 w-3.5" /> Теги
          </button>
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
