import { Plus } from 'lucide-react'
import { Button } from './button'

interface AddButtonProps {
  label?: string
  onClick: () => void
}

export function AddButton({ label = 'Добавить', onClick }: AddButtonProps) {
  return (
    <Button onClick={onClick}>
      <Plus className="h-4 w-4" /> {label}
    </Button>
  )
}
