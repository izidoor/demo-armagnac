import { cn } from '@/lib/utils'
import {
  AlertDialog,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import { Button } from '@/components/ui/button'

type ConfirmDialogProps = {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: React.ReactNode
  disabled?: boolean
  desc: React.JSX.Element | string
  cancelBtnText?: string
  confirmText?: React.ReactNode
  destructive?: boolean
  handleConfirm: () => void
  isLoading?: boolean
  className?: string
  /** Sous 768 px, une feuille collée en bas de l'écran, qui monte du bord, plutôt qu'une fenêtre au centre. */
  feuilleMobile?: boolean
  children?: React.ReactNode
}

/**
 * La feuille du bas : pleine largeur, coins arrondis en haut, au-dessus de la barre système (zone sûre) ; ses boutons
 * l'un sous l'autre, en pleine largeur, la confirmation en premier (pied d'`AlertDialog`).
 */
const FEUILLE_MOBILE = cn(
  'max-md:top-auto max-md:bottom-0 max-md:left-0 max-md:max-w-none max-md:translate-x-0 max-md:translate-y-0',
  'max-md:rounded-t-2xl max-md:rounded-b-none max-md:border-x-0 max-md:border-b-0 max-md:pt-3 max-md:pb-[calc(1.5rem+env(safe-area-inset-bottom))]',
  'max-md:data-[state=open]:slide-in-from-bottom max-md:data-[state=closed]:slide-out-to-bottom max-md:data-[state=open]:zoom-in-100 max-md:data-[state=closed]:zoom-out-100 max-md:duration-300'
)

export function ConfirmDialog(props: ConfirmDialogProps) {
  const {
    title,
    desc,
    children,
    className,
    confirmText,
    cancelBtnText,
    destructive,
    isLoading,
    disabled = false,
    handleConfirm,
    feuilleMobile = false,
    ...actions
  } = props
  return (
    <AlertDialog {...actions}>
      <AlertDialogContent
        className={cn(className, feuilleMobile && FEUILLE_MOBILE)}
      >
        {feuilleMobile && (
          <div className='bg-muted-foreground/30 mx-auto h-1.5 w-10 rounded-full md:hidden' />
        )}
        <AlertDialogHeader className='text-start'>
          <AlertDialogTitle>{title}</AlertDialogTitle>
          <AlertDialogDescription asChild>
            <div>{desc}</div>
          </AlertDialogDescription>
        </AlertDialogHeader>
        {children}
        <AlertDialogFooter>
          <AlertDialogCancel disabled={isLoading}>
            {cancelBtnText ?? 'Annuler'}
          </AlertDialogCancel>
          <Button
            variant={destructive ? 'destructive' : 'default'}
            onClick={handleConfirm}
            disabled={disabled || isLoading}
          >
            {confirmText ?? 'Continuer'}
          </Button>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  )
}
