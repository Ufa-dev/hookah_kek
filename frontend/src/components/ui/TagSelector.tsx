import { useState, useRef, useEffect } from 'react'
import { Plus, X } from 'lucide-react'
import type { Tag } from '@/types'

interface TagSelectorProps {
  assigned: Tag[]
  allTags: Tag[]
  onAdd: (tag: Tag) => void
  onRemove: (tag: Tag) => void
  disabled?: boolean
}

export function TagSelector({ assigned, allTags, onAdd, onRemove, disabled }: TagSelectorProps) {
  const [open, setOpen] = useState(false)
  const [query, setQuery] = useState('')
  const ref = useRef<HTMLDivElement>(null)
  const available = allTags.filter(t => !assigned.find(a => a.id === t.id) && t.name.toLowerCase().includes(query.toLowerCase()))

  useEffect(() => {
    const h = (e: MouseEvent) => { if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false) }
    document.addEventListener('mousedown', h)
    return () => document.removeEventListener('mousedown', h)
  }, [])

  return (
    <div className="space-y-2">
      <div className="flex flex-wrap gap-2 min-h-[2rem]">
        {assigned.map(t => (
          <span key={t.id} className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-gold/10 border border-gold/30 text-xs text-gold">
            {t.name}
            {!disabled && (
              <button onClick={() => onRemove(t)} className="hover:text-crimson transition-colors">
                <X className="h-2.5 w-2.5" />
              </button>
            )}
          </span>
        ))}
        {assigned.length === 0 && <p className="text-xs text-[#888]">Нет тегов</p>}
      </div>
      {!disabled && (
        <div ref={ref} className="relative">
          <button
            onClick={() => setOpen(o => !o)}
            className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border border-dashed border-border text-xs text-ink-dim hover:border-gold hover:text-gold transition-colors"
          >
            <Plus className="h-3 w-3" /> Добавить тег
          </button>
          {open && (
            <div className="absolute z-50 top-full mt-1 w-56 bg-[#1a1a1a] border border-[#2a2a2a] rounded-lg shadow-lg">
              <div className="p-2 border-b border-[#2a2a2a]">
                <input
                  autoFocus
                  className="w-full bg-transparent text-xs font-body text-[#f5f5f5] placeholder-[#666] outline-none"
                  placeholder="Поиск тега..."
                  value={query}
                  onChange={e => setQuery(e.target.value)}
                />
              </div>
              <div className="max-h-40 overflow-y-auto">
                {available.length === 0 ? (
                  <p className="px-3 py-2 text-xs text-[#888]">Теги не найдены</p>
                ) : available.map(t => (
                  <button
                    key={t.id}
                    className="w-full text-left px-3 py-1.5 text-xs font-body text-[#f5f5f5] hover:bg-[#252525] transition-colors"
                    onClick={() => { onAdd(t); setOpen(false); setQuery('') }}
                  >
                    {t.name}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
