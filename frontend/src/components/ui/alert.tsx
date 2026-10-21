import * as React from 'react'
import { cva, type VariantProps } from 'class-variance-authority'
import { cn } from '@/lib/utils'

const alertVariants = cva(
  'relative w-full rounded-lg border px-4 py-4 text-sm grid has-[>svg]:grid-cols-[calc(var(--spacing)*6)_1fr] grid-cols-[0_1fr] has-[>svg]:gap-x-3 gap-y-1 items-start [&>svg]:size-6 [&>svg]:-translate-y-0.5 [&>svg]:text-current',
  {
    variants: {
      /**
       * Style B de la page d'essai (essai du 2026-10-21, après les styles C puis E) : fond de carte, contour épais
       * de la couleur de la nature, icône et titre de cette couleur, texte courant.
       */
      variant: {
        /** Une information, une explication. */
        default:
          'bg-card text-card-foreground border-2 border-info-foreground/70 [&>svg]:text-info-foreground *:data-[slot=alert-title]:text-info-foreground',
        /** Ce qui a réussi, ce qui est en ordre. */
        success:
          'bg-card text-card-foreground border-2 border-success-foreground/70 [&>svg]:text-success-foreground *:data-[slot=alert-title]:text-success-foreground',
        /** Un manque à combler, non bloquant pour l'écran (fiche non facturable…). */
        warning:
          'bg-card text-card-foreground border-2 border-warning-foreground/70 [&>svg]:text-warning-foreground *:data-[slot=alert-title]:text-warning-foreground',
        /** Une erreur, un blocage. */
        destructive:
          'bg-card text-card-foreground border-2 border-danger-foreground/70 [&>svg]:text-danger-foreground *:data-[slot=alert-title]:text-danger-foreground',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  }
)

function Alert({
  className,
  variant,
  ...props
}: React.ComponentProps<'div'> & VariantProps<typeof alertVariants>) {
  return (
    <div
      data-slot='alert'
      role='alert'
      className={cn(alertVariants({ variant }), className)}
      {...props}
    />
  )
}

function AlertTitle({ className, ...props }: React.ComponentProps<'div'>) {
  return (
    <div
      data-slot='alert-title'
      className={cn(
        'col-start-2 line-clamp-1 min-h-5 font-semibold tracking-tight',
        className
      )}
      {...props}
    />
  )
}

function AlertDescription({
  className,
  ...props
}: React.ComponentProps<'div'>) {
  return (
    <div
      data-slot='alert-description'
      className={cn(
        'text-muted-foreground col-start-2 grid justify-items-start gap-1 text-sm [&_p]:leading-relaxed',
        className
      )}
      {...props}
    />
  )
}

export { Alert, AlertTitle, AlertDescription }
