import { forwardRef, type ButtonHTMLAttributes } from 'react'
import { Slot } from '@radix-ui/react-slot'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const buttonVariants = cva(
  [
    'inline-flex items-center justify-center gap-2 whitespace-nowrap',
    'font-body font-medium tracking-wide transition-all duration-150',
    'disabled:pointer-events-none disabled:opacity-40 select-none',
    'touch-target cursor-pointer',
    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red focus-visible:ring-offset-2',
  ].join(' '),
  {
    variants: {
      variant: {
        primary:
          'bg-red text-white hover:bg-red-dim active:scale-[0.97] rounded shadow-red-sm',
        ghost:
          'text-red hover:bg-red-pale border border-transparent hover:border-red-glow rounded',
        outline:
          'border border-border text-ink-dim hover:border-red hover:text-red bg-white rounded',
        danger:
          'border border-red-glow text-red hover:bg-red-pale bg-transparent rounded',
        link: 'text-red underline-offset-4 hover:underline p-0 h-auto rounded-none',
      },
      size: {
        default: 'h-11 px-5 text-sm',
        sm:      'h-9 px-4 text-xs',
        lg:      'h-12 px-7 text-base',
        icon:    'h-10 w-10 rounded',
        'icon-sm': 'h-8 w-8 rounded',
      },
    },
    defaultVariants: { variant: 'primary', size: 'default' },
  },
)

export interface ButtonProps
  extends ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean
}

const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : 'button'
    return (
      <Comp className={cn(buttonVariants({ variant, size, className }))} ref={ref} {...props} />
    )
  },
)
Button.displayName = 'Button'

export { Button, buttonVariants }
