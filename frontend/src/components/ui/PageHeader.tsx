import { AddButton } from './AddButton'

interface PageHeaderProps {
  title: string
  onAdd?: () => void
  addLabel?: string
}

export function PageHeader({ title, onAdd, addLabel = 'Добавить' }: PageHeaderProps) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-4 mb-8">
      <div>
        <p className="text-xs text-ink-muted font-body uppercase tracking-widest mb-1">
          Администрирование
        </p>
        <h1 className="font-display text-3xl sm:text-5xl font-bold text-ink">
          {title}
        </h1>
        <div className="red-line w-20 mt-3" />
      </div>
      {onAdd && <AddButton label={addLabel} onClick={onAdd} />}
    </div>
  )
}
