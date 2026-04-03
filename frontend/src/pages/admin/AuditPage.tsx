import { useState, useRef, useEffect } from 'react'
import { useInfiniteQuery } from '@tanstack/react-query'
import { auditApi } from '@/lib/api'
import { Badge } from '@/components/ui/badge'
import { PageHeader } from '@/components/ui/PageHeader'
import type { AuditEventType, BrandAuditRecord, FlavorAuditRecord, PackAuditRecord, MarketAuditRecord, Slice } from '@/types'
import { ChevronUp, ChevronDown } from 'lucide-react'
import { formatDate } from '@/lib/utils'

type Subdomain = 'brand' | 'flavor' | 'pack' | 'market'
type SortDir = 'asc' | 'desc'

const SUBDOMAIN_OPTIONS: { value: Subdomain; label: string }[] = [
  { value: 'brand',  label: 'Бренды' },
  { value: 'flavor', label: 'Вкусы' },
  { value: 'pack',   label: 'Контейнеры' },
  { value: 'market', label: 'Склад' },
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

// ─── Market table ─────────────────────────────────────────────────────────────

function MarketTable({ rows }: { rows: MarketAuditRecord[] }) {
  return (
    <>
      {rows.map(r => (
        <tr key={r.id} className="border-b border-border hover:bg-hover transition-colors">
          <td className="px-3 py-2.5"><EventBadge type={r.eventType} /></td>
          <td className="px-3 py-2.5 text-sm text-ink font-medium">{r.name}</td>
          <td className="px-3 py-2.5 text-xs text-ink-dim whitespace-nowrap">{r.weightGrams} г</td>
          <td className="px-3 py-2.5 text-xs text-ink-dim">{r.count}</td>
          <td className="px-3 py-2.5 font-mono text-xs text-ink-muted hidden sm:table-cell">
            {r.gtin ?? <span className="text-ink-muted">—</span>}
          </td>
          <td className="px-3 py-2.5 text-xs text-ink-muted"><TruncatedId value={r.updatedBy} /></td>
          <td className="px-3 py-2.5 text-xs text-ink-muted whitespace-nowrap">{formatDate(r.updatedAt)}</td>
        </tr>
      ))}
    </>
  )
}

function MarketHeader() {
  return (
    <tr className="bg-elevated border-b border-border">
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider w-[100px]">Тип</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Название</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Вес</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Кол-во</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider hidden sm:table-cell">GTIN</th>
      <th className="px-3 py-2.5 text-left text-xs font-body font-semibold text-ink-dim uppercase tracking-wider">Автор</th>
    </tr>
  )
}

// ─── Skeleton ─────────────────────────────────────────────────────────────────

function SkeletonRows({ cols }: { cols: number }) {
  return (
    <>
      {Array.from({ length: 5 }).map((_, i) => (
        <tr key={i} className="border-b border-border">
          {Array.from({ length: cols }).map((_, j) => (
            <td key={j} className="px-3 py-3">
              <div className="h-4 rounded bg-surface animate-pulse" style={{ width: j === 0 ? '72px' : '100%' }} />
            </td>
          ))}
        </tr>
      ))}
    </>
  )
}

// ─── Page ─────────────────────────────────────────────────────────────────────

export default function AuditPage() {
  const [subdomain, setSubdomain] = useState<Subdomain>('brand')
  const [eventTypeFilter, setEventTypeFilter] = useState<string>('')
  const [sortDir, setSortDir] = useState<SortDir>('desc')
  const sentinelRef = useRef<HTMLDivElement>(null)

  type AuditRecord = BrandAuditRecord | FlavorAuditRecord | PackAuditRecord

  const queryFn = ({ pageParam }: { pageParam: string }): Promise<Slice<AuditRecord>> => {
    const params = {
      limit: 30,
      after: pageParam || undefined,
      eventType: (eventTypeFilter || undefined) as AuditEventType | undefined,
    }
    if (subdomain === 'brand')  return auditApi.listBrand(params) as Promise<Slice<AuditRecord>>
    if (subdomain === 'flavor') return auditApi.listFlavor(params) as Promise<Slice<AuditRecord>>
    return auditApi.listPack(params) as Promise<Slice<AuditRecord>>
  }

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useInfiniteQuery({
    queryKey: ['audit-infinite', subdomain, eventTypeFilter],
    queryFn,
    initialPageParam: '',
    getNextPageParam: (last) => last.nextToken || undefined,
    enabled: subdomain !== 'market',
  })

  const marketAuditQuery = useInfiniteQuery({
    queryKey: ['audit-market-infinite', eventTypeFilter],
    queryFn: ({ pageParam }) =>
      auditApi.listMarket({ after: pageParam || undefined, eventType: (eventTypeFilter || undefined) as AuditEventType | undefined }),
    getNextPageParam: (last) => last.nextToken || undefined,
    initialPageParam: '',
    enabled: subdomain === 'market',
  })
  const marketItems = marketAuditQuery.data?.pages.flatMap(p => p.items) ?? []

  const activeHasNextPage = subdomain === 'market' ? marketAuditQuery.hasNextPage : hasNextPage
  const activeIsFetchingNextPage = subdomain === 'market' ? marketAuditQuery.isFetchingNextPage : isFetchingNextPage
  const activeFetchNextPage = subdomain === 'market' ? marketAuditQuery.fetchNextPage : fetchNextPage

  // IntersectionObserver sentinel
  useEffect(() => {
    const el = sentinelRef.current
    if (!el) return
    const obs = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && activeHasNextPage && !activeIsFetchingNextPage) {
          activeFetchNextPage()
        }
      },
      { threshold: 0.1 },
    )
    obs.observe(el)
    return () => obs.disconnect()
  }, [activeHasNextPage, activeIsFetchingNextPage, activeFetchNextPage])

  const allRows = data?.pages.flatMap(p => p.items) ?? []

  const sorted = [...allRows].sort((a, b) => {
    const cmp = a.updatedAt.localeCompare(b.updatedAt)
    return sortDir === 'asc' ? cmp : -cmp
  })

  const sortedMarket = [...marketItems].sort((a, b) => {
    const cmp = a.updatedAt.localeCompare(b.updatedAt)
    return sortDir === 'asc' ? cmp : -cmp
  })

  const activeIsLoading = subdomain === 'market' ? marketAuditQuery.isLoading : isLoading
  const activeIsEmpty = subdomain === 'market' ? sortedMarket.length === 0 : sorted.length === 0

  const SortIcon = sortDir === 'asc' ? ChevronUp : ChevronDown

  return (
    <div className="page-root">
      <div className="page-container page-enter">

        {/* Header */}
        <PageHeader title="Аудит" />

        {/* Subdomain tab pills */}
        <div className="flex gap-2 mb-4">
          {SUBDOMAIN_OPTIONS.map(o => (
            <button
              key={o.value}
              onClick={() => setSubdomain(o.value)}
              className={
                subdomain === o.value
                  ? 'px-4 py-1.5 rounded-full text-sm font-body font-semibold bg-red text-white transition-colors'
                  : 'px-4 py-1.5 rounded-full text-sm font-body border border-border text-ink-dim hover:bg-hover transition-colors'
              }
            >
              {o.label}
            </button>
          ))}
        </div>

        {/* Event type filter pills + sort */}
        <div className="flex flex-wrap items-center justify-between gap-3 mb-6">
          <div className="flex flex-wrap gap-2 items-center">
            <span className="text-xs text-ink-muted font-body">Событие:</span>
            {EVENT_TYPE_OPTIONS.map(o => (
              <button
                key={o.value}
                onClick={() => setEventTypeFilter(o.value)}
                className={
                  eventTypeFilter === o.value
                    ? 'inline-flex items-center px-2 py-1 rounded-full bg-gold/10 border border-gold/30 text-xs text-gold transition-colors'
                    : 'inline-flex items-center px-2 py-1 rounded-full border border-border text-xs text-ink-dim hover:border-gold/40 hover:text-gold transition-colors'
                }
              >
                {o.label}
              </button>
            ))}
          </div>
          <button
            onClick={() => setSortDir(d => d === 'asc' ? 'desc' : 'asc')}
            className="inline-flex items-center gap-1 text-xs font-body font-semibold text-ink-dim uppercase tracking-wider hover:text-red transition-colors"
          >
            Дата <SortIcon className="h-3.5 w-3.5" />
          </button>
        </div>

        {/* Table */}
        <div className="overflow-x-auto rounded border border-border">
          <table className="w-full border-collapse text-left">
            <thead>
              {subdomain === 'brand'  && <BrandHeader />}
              {subdomain === 'flavor' && <FlavorHeader />}
              {subdomain === 'pack'   && <PackHeader />}
              {subdomain === 'market' && <MarketHeader />}
            </thead>
            <tbody>
              {activeIsLoading && <SkeletonRows cols={subdomain === 'brand' ? 5 : 7} />}
              {!activeIsLoading && activeIsEmpty && (
                <tr>
                  <td colSpan={8} className="px-3 py-6 text-center text-sm text-ink-muted">Нет записей</td>
                </tr>
              )}
              {subdomain === 'brand'  && <BrandTable  rows={sorted as BrandAuditRecord[]} />}
              {subdomain === 'flavor' && <FlavorTable rows={sorted as FlavorAuditRecord[]} />}
              {subdomain === 'pack'   && <PackTable   rows={sorted as PackAuditRecord[]} />}
              {subdomain === 'market' && <MarketTable rows={sortedMarket} />}
            </tbody>
          </table>
        </div>

        {activeIsFetchingNextPage && (
          <div className="mt-4 flex justify-center">
            <div className="h-4 w-32 rounded bg-surface animate-pulse" />
          </div>
        )}

        <div ref={sentinelRef} className="h-4" />
      </div>
    </div>
  )
}
