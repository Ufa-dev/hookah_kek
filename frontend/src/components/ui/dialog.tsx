import { type HTMLAttributes } from 'react'
import * as DialogPrimitive from '@radix-ui/react-dialog'
import { X } from 'lucide-react'
import { cn } from '@/lib/utils'

const Dialog        = DialogPrimitive.Root
const DialogTrigger = DialogPrimitive.Trigger
const DialogClose   = DialogPrimitive.Close
const DialogPortal  = DialogPrimitive.Portal

function DialogOverlay({ className, ...props }: DialogPrimitive.DialogOverlayProps) {
  return (
    <DialogPrimitive.Overlay
      className={cn(
        'fixed inset-0 z-50 bg-black/30 backdrop-blur-sm',
        'data-[state=open]:animate-in data-[state=closed]:animate-out',
        'data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0',
        className,
      )}
      {...props}
    />
  )
}

function DialogContent({ className, children, ...props }: DialogPrimitive.DialogContentProps) {
  return (
    <DialogPortal>
      <DialogOverlay />
      <DialogPrimitive.Content
        className={cn(
          'fixed left-1/2 top-1/2 z-50 -translate-x-1/2 -translate-y-1/2',
          'w-[calc(100vw-2rem)] max-w-lg sm:w-full',
          'bg-[#161616] border border-[#2a2a2a] rounded-xl',
          'shadow-card-lg',
          'data-[state=open]:animate-in data-[state=closed]:animate-out',
          'data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0',
          'data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95',
          'data-[state=closed]:slide-out-to-top-2 data-[state=open]:slide-in-from-top-2',
          className,
        )}
        {...props}
      >
        {/* Red top accent — own overflow-hidden so it clips to rounded corners */}
        <div className="overflow-hidden rounded-t-xl">
          <div className="red-line" />
        </div>
        <div className="p-5 sm:p-6">{children}</div>
        <DialogPrimitive.Close
          className="absolute right-4 top-4 p-1.5 rounded text-[#555] hover:text-[#f5f5f5] hover:bg-[#252525] transition-colors touch-target flex items-center justify-center"
          aria-label="Закрыть"
        >
          <X className="h-4 w-4" />
        </DialogPrimitive.Close>
      </DialogPrimitive.Content>
    </DialogPortal>
  )
}

function DialogHeader({ className, ...props }: HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('mb-5 pr-6', className)} {...props} />
}

function DialogTitle({ className, ...props }: DialogPrimitive.DialogTitleProps) {
  return (
    <DialogPrimitive.Title
      className={cn('font-display text-xl sm:text-2xl text-[#f5f5f5]', className)}
      {...props}
    />
  )
}

function DialogDescription({ className, ...props }: DialogPrimitive.DialogDescriptionProps) {
  return (
    <DialogPrimitive.Description
      className={cn('text-sm text-[#555] mt-1', className)}
      {...props}
    />
  )
}

export {
  Dialog, DialogTrigger, DialogClose, DialogPortal,
  DialogOverlay, DialogContent, DialogHeader,
  DialogTitle, DialogDescription,
}
