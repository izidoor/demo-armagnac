import { Link } from '@tanstack/react-router'
import { ExternalLink } from 'lucide-react'
import { type ObjetConversation } from '@/api/generated'
import { Button } from '@/components/ui/button'

/** Où s'affiche la messagerie : le lien vers l'objet dépend de l'espace (spec 15 §3.2). */
export type EspaceMessagerie = 'bo' | 'portail'

/**
 * Le lien vers l'objet d'une conversation, construit du type et de l'identifiant selon l'espace : il n'est pas
 * stocké (spec 15 §3.2).
 */
export function LienObjet({
  objet,
  operateur,
  espace,
}: {
  objet: ObjetConversation
  operateur: number
  espace: EspaceMessagerie
}) {
  const libelle = objet.type === 'DSM' ? 'Ouvrir la DSM' : 'Ouvrir la facture'
  const icone = <ExternalLink className='size-4' />
  if (objet.type === 'DSM') {
    return (
      <Button size='sm' asChild>
        {espace === 'bo' ? (
          <Link
            to='/bo/operateurs/$id/dsm/$dsm'
            params={{ id: String(operateur), dsm: objet.identifiant }}
          >
            {icone} {libelle}
          </Link>
        ) : (
          <Link
            to='/portail/declarations/$dsm'
            params={{ dsm: objet.identifiant }}
          >
            {icone} {libelle}
          </Link>
        )}
      </Button>
    )
  }
  return (
    <Button size='sm' asChild>
      {espace === 'bo' ? (
        <Link to='/bo/facturation/$id' params={{ id: objet.identifiant }}>
          {icone} {libelle}
        </Link>
      ) : (
        <Link to='/portail/factures/$id' params={{ id: objet.identifiant }}>
          {icone} {libelle}
        </Link>
      )}
    </Button>
  )
}
