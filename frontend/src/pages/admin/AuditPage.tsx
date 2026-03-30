import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { auditApi } from '@/lib/api'
import { Badge } from '@/components/ui/badge'
import type { AuditEventType, BrandAuditRecord, FlavorAuditRecord, PackAuditRecord } from '@/types'
import { ChevronUp, ChevronDown } from 'lucide-react'
import { formatDate } from '@/lib/utils'

type Subdomain = 'brand' | 'flavor' | 'pack'
type SortDir = 'asc' | 'desc'

const SUBDOMAIN_OPTIONS: { value: Subdomain; label: string }[] = [
  { value: 'brand',  label: 'Бренды' },
  { value: 'flavor', label: 'Вкусы' },
  { value: 'pack',   label: 'Контейнеры' },
]

const EVENT_TYPE_OPTIONS: { value: string; label: string }[] = [
  { value: '',        label: 'Все' },
  { value: 'CREATED', label: 'Создание' },
  { value: 'UPDATED', label: 'Изменение' },
  { value: 'DELETED', label: 'Удаление' },
]

function EventBadge({ type }: { type: AuditEventType }) {
  if (type === 'CREATED') return <Badge variant="outline" className="text-gold border-gold/40">Создано</Badge>
  if (type === 'UPDATED') return <Badge variant="surface">Изменено</Badge>
  return <Badge>Удалено</Badge>
}

function TruncatedId({ value }: { value: string }) {
  return (
    <span className="truncate max-w-[120px] inline-block align-bottom" title={value}>
      {value}
    </span>
  )
}

// ─── Brand table ──────────────────────────────────────────────────────────────

function BrandTable({ rows }: { rows: BrandAuditRecord[] }) {
  return (
    <>
      {rows.map(r => (
        <tr key={r.id} className="border-b border-border hover:bg-hover transition-colors">
          <td className="px-3 py-2.5"><EventBadge type={r.eventType} /></td>
          <td className="px-3 py-2.5 text-sm text-ink font-medium">{r.name}</td>
          <td className="px-3 py-2.5 text-sm text-ink-dim hidden sm:table-cell">
            {r.description ?? <span className="text-ink-muted">—</span>}
          </td>
          <td className="px-3 py-2.5 text-xs text-ink-muted"><TruncatedId value={r.updatedBy} /></td>
          <td className="px-3 py-2.5 text-xs text-ink-muted whitespace-nowrap">{formatDate(r.updatedAt)}</td>
        </tr>
      ))}
    </>
  )
}

function BrandHeader() {
  return (
    <tr className="bg-elevated border-b border-border">
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider w-[100px]">Тип</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Название</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider hidden sm:table-cell">Описание</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Автор</th>
    </tr>
  )
}

// ─── Flavor table ─────────────────────────────────────────────────────────────

function FlavorTable({ rows }: { rows: FlavorAuditRecord[] }) {
  return (
    <>
      {rows.map(r => (
        <tr key={r.id} className="border-b border-border hover:bg-hover transition-colors">
          <td className="px-3 py-2.5"><EventBadge type={r.eventType} /></td>
          <td className="px-3 py-2.5 text-sm text-ink font-medium">{r.name}</td>
          <td className="px-3 py-2.5 text-xs text-ink-muted hidden sm:table-cell"><TruncatedId value={r.brandId} /></td>
          <td className="px-3 py-2.5 text-xs text-ink-dim">
            {r.strength !== null ? r.strength : <span className="text-ink-muted">—</span>}
          </td>
          <td className="px-3 py-2.5 text-sm text-ink-dim hidden sm:table-cell">
            {r.description ?? <span className="text-ink-muted">—</span>}
          </td>
          <td className="px-3 py-2.5 text-xs text-ink-muted"><TruncatedId value={r.updatedBy} /></td>
          <td className="px-3 py-2.5 text-xs text-ink-muted whitespace-nowrap">{formatDate(r.updatedAt)}</td>
        </tr>
      ))}
    </>
  )
}

function FlavorHeader() {
  return (
    <tr className="bg-elevated border-b border-border">
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider w-[100px]">Тип</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Название</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider hidden sm:table-cell">Бренд</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Крепость</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider hidden sm:table-cell">Описание</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Автор</th>
    </tr>
  )
}

// ─── Pack table ───────────────────────────────────────────────────────────────

function PackTable({ rows }: { rows: PackAuditRecord[] }) {
  return (
    <>
      {rows.map(r => (
        <tr key={r.id} className="border-b border-border hover:bg-hover transition-colors">
          <td className="px-3 py-2.5"><EventBadge type={r.eventType} /></td>
          <td className="px-3 py-2.5 text-xs text-ink-muted"><TruncatedId value={r.packId} /></td>
          <td className="px-3 py-2.5 text-sm text-ink font-medium">{r.name}</td>
          <td className="px-3 py-2.5 text-xs text-ink-muted hidden sm:table-cell">
            {r.flavorId ? <TruncatedId value={r.flavorId} /> : <span className="text-ink-muted">—</span>}
          </td>
          <td className="px-3 py-2.5 text-xs text-ink-dim whitespace-nowrap">
            {r.currentWeightGrams} / {r.totalWeightGrams} г
          </td>
          <td className="px-3 py-2.5 text-xs text-ink-muted"><TruncatedId value={r.updatedBy} /></td>
          <td className="px-3 py-2.5 text-xs text-ink-muted whitespace-nowrap">{formatDate(r.updatedAt)}</td>
        </tr>
      ))}
    </>
  )
}

function PackHeader() {
  return (
    <tr className="bg-elevated border-b border-border">
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider w-[100px]">Тип</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Контейнер</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Название</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider hidden sm:table-cell">Вкус</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Вес (тек./общ.)</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Автор</th>
    </tr>
  )
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function AuditPage() {
  const [subdomain, setSubdomain] = useState<Subdomain>('brand')
  const [eventTypeFilter, setEventTypeFilter] = useState<string>('')
  const [sortDir, setSortDir] = useState<SortDir>('desc')
  const sentinelRef = useRef<HTMLDivElement>(null)

  const queryFn = ({ pageParam }: { pageParam: string }) => {
    const params = {
      limit: 30,
      after: pageParam || undefined,
      eventType: (eventTypeFilter || undefined) as AuditEventType | undefined,
    }
    if (subdomain === 'brand')  return auditApi.listBrand(params)
    if (subdomain === 'flavor') return auditApi.listFlavor(params)
    return auditApi.listPack(params)
  }

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useInfiniteQuery({
    queryKey: ['audit-infinite', subdomain, eventTypeFilter],
    queryFn,
    initialPageParam: '',
    getNextPageParam: (last) => last.nextToken || undefined,
  })

  // IntersectionObserver sentinel
  useEffect(() => {
    const el = sentinelRef.current
    if (!el) return
    const obs = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasNextPage && !isFetchingNextPage) {
          fetchNextPage()
        }
      },
      { threshold: 0.1 },
    )
    obs.observe(el)
    return () => obs.disconnect()
  }, [hasNextPage, isFetchingNextPage, fetchNextPage])

  const allRows = data?.pages.flatMap(p => p.items) ?? []

  const sorted = [...allRows].sort((a, b) => {
    const cmp = a.updatedAt.localeCompare(b.updatedAt)
    return sortDir === 'asc' ? cmp : -cmp
  })

  const SortIcon = sortDir === 'asc' ? ChevronUp : ChevronDown

  return (
    <div className="page-root">
      <div className="page-container page-enter">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h1 className="font-display text-2xl text-ink">Аудит</h1>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap gap-3 mb-6">
          <div className="field">
            <select
              value={subdomain}
              onChange={e => setSubdomain(e.target.value as Subdomain)}
              className="field text-sm text-ink bg-surface border border-border rounded px-3 py-2 focus:outline-none focus:border-red-light"
            >
              {SUBDOMAIN_OPTIONS.map(o => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>
          <div className="field">
            <select
              value={eventTypeFilter}
              onChange={e => setEventTypeFilter(e.target.value)}
              className="field text-sm text-ink bg-surface border border-border rounded px-3 py-2 focus:outline-none focus:border-red-light"
            >
              {EVENT_TYPE_OPTIONS.map(o => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto rounded border border-border">
          <table className="w-full border-collapse text-left">
            <thead>
              {subdomain === 'brand'  && <BrandHeader />}
              {subdomain === 'flavor' && <FlavorHeader />}
              {subdomain === 'pack'   && <PackHeader />}
              <tr className="bg-elevated border-b border-border">
                {/* Date header with sort — spans last column */}
                <th
                  colSpan={100}
                  className="px-3 py-2.5 text-right"
                >
                  <button
                    onClick={() => setSortDir(d => d === 'asc' ? 'desc' : 'asc')}
                    className="inline-flex items-center gap-1 text-xs font-body font-semibold text-ink-dim uppercase tracking-wider hover:text-red transition-colors"
                  >
                    Дата <SortIcon className="h-3.5 w-3.5" />
                  </button>
                </th>
              </tr>
            </thead>
            <tbody>
              {isLoading && (
                <tr>
                  <td colSpan={8} className="px-3 py-6 text-center text-sm text-ink-muted">Загрузка…</td>
                </tr>
              )}
              {!isLoading && sorted.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-3 py-6 text-center text-sm text-ink-muted">Нет записей</td>
                </tr>
              )}
              {subdomain === 'brand'  && <BrandTable  rows={sorted as BrandAuditRecord[]} />}
              {subdomain === 'flavor' && <FlavorTable rows={sorted as FlavorAuditRecord[]} />}
              {subdomain === 'pack'   && <PackTable   rows={sorted as PackAuditRecord[]} />}
            </tbody>
          </table>
        </div>

        {isFetchingNextPage && (
          <p className="mt-4 text-center text-sm text-ink-muted">Загрузка…</p>
        )}

        {/* Sentinel for infinite scroll */}
        <div ref={sentinelRef} className="h-4" />
      </div>
    </div>
  )
}
