import { useQuery, useQueryClient } from '@tanstack/react-query'
import { MessagesSquare } from 'lucide-react'
import { Badge } from '@/components/ui/badge'
import {
  apiMessagerie,
  cleListesMessagerie,
  cleMessagerie,
  type TypeObjet,
} from './api'
import { FilConversation } from './fil-conversation'

/**
 * La section « Messages » de la page d'un objet (spec 15 §6) : sa conversation avec l'autre côté, la même que dans
 * la messagerie. L'afficher la lit pour mon côté (RG-MSG-10) ; elle naît au premier message (RG-MSG-04).
 */
export function SectionMessages({
  type,
  identifiant,
  description,
}: {
  type: TypeObjet
  identifiant: string
  description: string
}) {
  const queryClient = useQueryClient()
  const cle = [...cleMessagerie, 'objet', type, identifiant]
  const requete = useQuery({
    queryKey: cle,
    queryFn: () => apiMessagerie.conversationObjet(type, identifiant),
    // Chaque affichage lit la conversation (RG-MSG-10) : pas de relecture au retour sur l'onglet.
    refetchOnWindowFocus: false,
  })
  const conversation = requete.data
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: cleMessagerie })
  const listes = () =>
    queryClient.invalidateQueries({ queryKey: cleListesMessagerie })

  return (
    <section
      id='messages'
      className='bg-card mt-8 scroll-mt-4 rounded-md border p-5 shadow-xs'
    >
      <div className='mb-6 space-y-1.5 border-b pb-4'>
        <h2 className='flex items-center gap-2 text-xl font-semibold'>
          <MessagesSquare className='size-5' /> Messages
          {conversation && conversation.messages.length > 0 && (
            <Badge variant='secondary'>{conversation.messages.length}</Badge>
          )}
        </h2>
        <p className='text-muted-foreground text-sm'>{description}</p>
      </div>
      {conversation ? (
        <FilConversation
          conversation={conversation}
          ecrire={(texte) =>
            conversation.id != null
              ? apiMessagerie.repondre(conversation.id, texte)
              : apiMessagerie.ecrireSurObjet(type, identifiant, texte)
          }
          onChange={rafraichir}
          onNonLu={listes}
          invitation='Aucun message pour l’instant.'
        />
      ) : (
        <p className='text-muted-foreground text-sm'>
          {requete.isError
            ? 'Les messages n’ont pas pu être chargés.'
            : 'Chargement…'}
        </p>
      )}
    </section>
  )
}
