import React from 'react'

interface WeightBarProps {
  current: number
  total: number
}

function getState(pct: number): { barStyle: React.CSSProperties; badgeClass: string; label: string } {
  if (pct > 50) return {
    barStyle: { width: `${pct}%`, background: '#22c55e' },
    badgeClass: 'bg-green-50 border-green-200 text-green-700',
    label: 'в норме',
  }
  if (pct > 20) return {
    barStyle: { width: `${pct}%`, background: '#D4A647' },
    badgeClass: 'bg-yellow-50 border-yellow-200 text-yellow-700',
    label: 'заканчивается',
  }
  return {
    barStyle: { width: `${pct}%`, background: '#9B2335' },
    badgeClass: 'bg-red-50 border-red-200 text-red-700',
    label: 'критично',
  }
}

export function WeightBar({ current, total }: WeightBarProps) {
  const pct = total > 0 ? Math.round((current / total) * 100) : 0
  const { barStyle, badgeClass, label } = getState(pct)
  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between gap-2">
        <span className={`inline-flex items-center px-2 py-0.5 rounded-sm border text-xs font-body font-semibold ${badgeClass}`}>
          {pct}% — {label}
        </span>
        <span className="text-xs text-ink-muted font-body">{current} / {total} г</span>
      </div>
      <div className="h-2 rounded-sm overflow-hidden" style={{ background: '#E5E7EB', border: '1px solid #D1D5DB' }}>
        <div className="h-full rounded-sm transition-all duration-300" style={barStyle} />
      </div>
    </div>
  )
}
