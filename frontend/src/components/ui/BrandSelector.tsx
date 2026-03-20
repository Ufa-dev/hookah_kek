import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { brandApi } from '@/lib/api'
import type { TabacoBrand } from '@/types'
import { Search, X, ChevronDown } from 'lucide-react'

export function BrandSelector({
  selected,
  onSelect,
}: {
  selected: TabacoBrand | null
  onSelect: (b: TabacoBrand | null) => void
}) {
  const [query, setQuery] = useState('')
  const [open, setOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  const { data: brands } = useInfiniteQuery({
    queryKey: ['brands-selector', query],
    queryFn: async ({ pageParam }) => {
      if (query.length >= 2) {
        const res = await brandApi.findByName(query)
        return { data: Array.isArray(res) ? res : [res], nextCursor: '' }
      }
      const res = await brandApi.list({ limit: 20, after: pageParam || undefined })
      return { data: res.items, nextCursor: res.nextToken || '' }
    },
    getNextPageParam: (last) => last.nextCursor || undefined,
    initialPageParam: '',
  })

  const items = brands?.pages.flatMap(p => p.data) ?? []

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  return (
    <div ref={ref} className="relative w-full max-w-sm">
      <div
        className="flex items-center gap-2 px-3 py-2 rounded-lg border border-border bg-surface cursor-pointer hover:border-red transition-colors"
        onClick={() => setOpen(o => !o)}
      >
        <Search className="h-4 w-4 text-ink-muted flex-shrink-0" />
        {selected ? (
          <span className="text-sm font-body text-ink flex-1">{selected.name}</span>
        ) : (
          <input
            className="bg-transparent text-sm font-body text-ink placeholder-ink-muted outline-none flex-1"
            placeholder="Выберите бренд..."
            value={query}
            onChange={e => { setQuery(e.target.value); setOpen(true) }}
            onClick={e => { e.stopPropagation(); setOpen(true) }}
          />
        )}
        {selected ? (
          <button onClick={e => { e.stopPropagation(); onSelect(null); setQuery('') }}>
            <X className="h-3.5 w-3.5 text-ink-muted hover:text-red" />
          </button>
        ) : (
          <ChevronDown className="h-3.5 w-3.5 text-ink-muted" />
        )}
      </div>
      {open && items.length > 0 && (
        <div className="absolute z-50 top-full mt-1 w-full bg-elevated border border-border rounded-lg shadow-lg max-h-52 overflow-y-auto">
          {items.map(b => (
            <button
              key={b.id}
              className="w-full text-left px-3 py-2 text-sm font-body text-ink hover:bg-hover transition-colors"
              onClick={() => { onSelect(b); setOpen(false); setQuery('') }}
            >
              {b.name}
            </button>
          ))}
        </div>
      )}
    </div>
  )
}
