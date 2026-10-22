import { type ReactNode } from 'react'
import { ChevronLeft } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'

/**
 * Un formulaire de création (nouvel opérateur, contact, invitation) : une fenêtre sur un écran large, une pleine page
 * sur un téléphone ou une tablette en portrait (moins de 768 px, comme le tiroir de la barre latérale), entrée par la
 * droite, sous « Annuler » qui la referme. La page défile sous ce bouton, qui reste en haut.
 */
export function DialogueFormulaire({
  ouvert,
  onOuvertChange,
  titre,
  description,
  className,
  children,
}: {
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  titre: ReactNode
  description?: ReactNode
  /** La largeur de la fenêtre sur un écran large : `sm:max-w-3xl`. */
  className?: string
  children: ReactNode
}) {
  return (
    <Dialog open={ouvert} onOpenChange={onOuvertChange}>
      <DialogContent
        className={cn(
          'gap-0 p-0 md:max-h-[calc(100dvh-2rem)] md:overflow-y-auto',
          'max-md:inset-0 max-md:flex max-md:h-dvh max-md:max-w-none! max-md:translate-x-0 max-md:translate-y-0 max-md:flex-col max-md:overflow-y-auto max-md:overscroll-contain max-md:rounded-none max-md:border-0 max-md:shadow-none',
          'max-md:data-[state=closed]:slide-out-to-right max-md:data-[state=closed]:zoom-out-100 max-md:data-[state=open]:slide-in-from-right max-md:data-[state=open]:zoom-in-100',
          'max-md:[&>[data-slot=dialog-close]]:hidden',
          className
        )}
      >
        <div className='bg-background sticky top-0 z-10 flex h-14 shrink-0 items-center border-b px-2 md:hidden'>
          <DialogClose asChild>
            <Button variant='ghost' className='gap-1 px-2'>
              <ChevronLeft className='size-5' /> Annuler
            </Button>
          </DialogClose>
        </div>
        <div className='grid gap-4 p-6 max-md:p-4 max-md:pb-8'>
          <DialogHeader className='text-start'>
            <DialogTitle>{titre}</DialogTitle>
            {description && (
              <DialogDescription>{description}</DialogDescription>
            )}
          </DialogHeader>
          {children}
        </div>
      </DialogContent>
    </Dialog>
  )
}
