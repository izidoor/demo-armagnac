import { Fragment } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Separator } from '@/components/ui/separator'
import { PageMetier } from '@/components/metier/page-metier'
import { PageVendeur } from '@/features/facturation/page-vendeur'
import { useMoi } from '@/features/session/session'
import { apiOperateur, cleOperateur } from '../commun/api'
import { OngletAnnuaire } from '../personnes/annuaire'
import { OngletGeneral } from './onglet-general'

/**
 * Paramètres de l'application > BNIA (spec 02 §6.1) : la fiche réduite de l'opérateur BNIA, l'opérateur
 * actif (RG-OPE-39), ses contacts avec le journal des accès, et le vendeur des factures (spec 08 §6.1).
 */
export function SectionBnia() {
  const { actif } = useMoi()
  const id = actif?.operateur ?? 0
  const queryClient = useQueryClient()
  const requete = useQuery({
    queryKey: [...cleOperateur(id), 'fiche'],
    queryFn: apiOperateur(id).fiche,
    enabled: id > 0,
  })
  const fiche = requete.data
  const rafraichir = () => {
    toast.success('Enregistré.')
    queryClient.invalidateQueries({ queryKey: cleOperateur(id) })
    queryClient.invalidateQueries({ queryKey: ['operateurs'] })
  }

  return (
    <div className='pb-8'>
      {[
        <PageMetier
          key='fiche'
          titre='Fiche'
          description='Identité, coordonnées et désignations du BNIA.'
        >
          {fiche ? (
            <OngletGeneral
              espace='bo'
              fiche={fiche}
              onEnregistre={rafraichir}
            />
          ) : (
            <p className='text-muted-foreground text-sm'>
              {requete.isError
                ? "La fiche n'a pas pu être chargée."
                : 'Chargement…'}
            </p>
          )}
        </PageMetier>,
        <PageMetier
          key='contacts'
          titre='Contacts'
          description='Le personnel du BNIA et ses contacts.'
        >
          {fiche && (
            <OngletAnnuaire
              espace='bo'
              operateur={id}
              droits={fiche.droits}
              avecJournal
            />
          )}
        </PageMetier>,
        <PageVendeur key='facturation' />,
      ].map((enfant, i) => (
        <Fragment key={i}>
          {i > 0 && <Separator className='my-8' />}
          <section>{enfant}</section>
        </Fragment>
      ))}
    </div>
  )
}
