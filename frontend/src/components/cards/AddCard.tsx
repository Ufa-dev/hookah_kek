import { Plus } from 'lucide-react'

interface AddCardProps {
  label: string
  onClick: () => void
}

export function AddCard({ label, onClick }: AddCardProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="group flex flex-col items-center justify-center gap-3 min-h-[140px] w-full
                 rounded-lg transition-all duration-150 cursor-pointer
                 border-l border-r border-b border-[#2a2a2a] border-t-2 border-t-[#3a3a3a]
                 bg-[#0f0f0f]
                 hover:border-l-[#9B2335] hover:border-r-[#9B2335] hover:border-b-[#9B2335] hover:border-t-[#B91C1C] hover:bg-[#160909]"
      aria-label={label}
    >
      <div className="flex items-center justify-center w-10 h-10 rounded-lg
                      bg-[#1e1e1e] border border-[#333]
                      transition-all duration-150
                      group-hover:bg-[#9B233322] group-hover:border-[#9B2335]">
        <Plus className="h-5 w-5 text-[#888] group-hover:text-[#B91C1C] transition-colors duration-150" />
      </div>
      <div className="text-center">
        <p className="text-sm font-body font-medium text-[#aaa] group-hover:text-[#f5f5f5] transition-colors duration-150">
          {label}
        </p>
        <p className="text-xs font-body text-[#777] group-hover:text-[#999] transition-colors duration-150 mt-0.5">
          нажми чтобы добавить
        </p>
      </div>
    </button>
  )
}
