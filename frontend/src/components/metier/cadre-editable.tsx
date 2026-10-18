import { type ReactNode } from 'react'
import { Pencil } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardAction,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'

type CadreEditableProps = {
  titre: ReactNode
  /** Le cadre est en édition : son contenu montre les champs, son pied « Annuler » et « Enregistrer ». */
  edition: boolean
  /** Montre « Éditer » ; sans droit de modifier, le cadre reste en lecture. */
  modifiable: boolean
  /** Un autre cadre est en édition : « Éditer » est désactivé. */
  bloque?: boolean
  onEditer: () => void
  onAnnuler: () => void
  /** Sans gestionnaire, « Enregistrer » soumet le formulaire qui englobe le cadre. */
  onEnregistrer?: () => void
  enregistrement?: boolean
  className?: string
  children: ReactNode
}

/**
 * Un cadre de fiche affiché en lecture (libellés et valeurs) ; « Éditer » le bascule en saisie, avec
 * « Annuler » et « Enregistrer ».
 */
export function CadreEditable({
  titre,
  edition,
  modifiable,
  bloque = false,
  onEditer,
  onAnnuler,
  onEnregistrer,
  enregistrement = false,
  className,
  children,
}: CadreEditableProps) {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>{titre}</CardTitle>
        {modifiable && !edition && (
          <CardAction>
            <Button
              type='button'
              variant='outline'
              size='sm'
              disabled={bloque}
              onClick={onEditer}
            >
              <Pencil className='size-4' /> Éditer
            </Button>
          </CardAction>
        )}
      </CardHeader>
      <CardContent>{children}</CardContent>
      {edition && (
        <CardFooter className='justify-end gap-2'>
          <Button
            type='button'
            variant='outline'
            disabled={enregistrement}
            onClick={onAnnuler}
          >
            Annuler
          </Button>
          <Button
            type={onEnregistrer ? 'button' : 'submit'}
            disabled={enregistrement}
            onClick={onEnregistrer}
          >
            Enregistrer
          </Button>
        </CardFooter>
      )}
    </Card>
  )
}
