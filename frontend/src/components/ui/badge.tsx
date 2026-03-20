import { type HTMLAttributes } from 'react'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const badgeVariants = cva(
  'inline-flex items-center gap-1 font-body text-xs px-2.5 py-0.5 rounded-full border transition-colors font-medium',
  {
    variants: {
      variant: {
        red:     'bg-red-pale border-red-glow text-red-dim',
        outline: 'border-border text-ink-dim bg-transparent',
        surface: 'bg-elevated border-border text-ink-dim',
      },
    },
    defaultVariants: { variant: 'red' },
  },
)

export interface BadgeProps
  extends HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />
}

export { Badge, badgeVariants }
