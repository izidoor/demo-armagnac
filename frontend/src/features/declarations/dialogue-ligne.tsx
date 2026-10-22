import { type FormEvent, useState } from 'react'
import { Button } from '@/components/ui/button'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { ChampsIdentite } from './champs-identite'
import { millesimee } from './libelles'
import { type IdentiteSaisie, versApi } from './saisie-ligne'

/**
 * Ajouter une ligne à la DSM (RG-DEC-13) : la clé, puis le millésime ou la dénomination, et le compte
 * d'âge sauf pour MB, calculé. La ligne commence à 0 ; la vue provisoire signale un doublon (RG-DEC-11).
 */
export function DialogueLigne({
  onOpenChange,
  onAjoute,
}: {
  onOpenChange: (open: boolean) => void
  onAjoute: (ligne: ReturnType<typeof versApi>) => void
}) {
  const [identite, setIdentite] = useState<IdentiteSaisie>({
    cle: 'NB',
    designation: '',
    compte: '',
  })
  const complete =
    identite.designation.trim() !== '' &&
    (identite.cle === 'MB' || identite.compte.trim() !== '') &&
    (!millesimee(identite.cle) || /^\d{4}$/.test(identite.designation.trim()))
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    onAjoute(versApi(identite))
    onOpenChange(false)
  }
  return (
    <DialogueFormulaire
      ouvert
      onOuvertChange={onOpenChange}
      className='sm:max-w-3xl'
      titre='Ajouter une ligne'
      description="Une ligne millésimée (MB, MV) porte un millésime ; un assemblage (NB, NV), une dénomination. Le compte d'âge d'une ligne MB se calcule depuis son millésime."
    >
      <form onSubmit={soumettre} className='space-y-4'>
        <div className='grid gap-4 sm:grid-cols-[1fr_1.3fr_0.7fr]'>
          <ChampsIdentite etiquettes valeur={identite} onChange={setIdentite} />
        </div>
        <div className='flex justify-end'>
          <Button type='submit' className='max-md:w-full' disabled={!complete}>
            Ajouter
          </Button>
        </div>
      </form>
    </DialogueFormulaire>
  )
}
