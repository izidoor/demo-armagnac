import { PageMetier } from '@/components/metier/page-metier'

/** Le portail sans opérateur : hors profil demo, tant que acces n'identifie pas la personne. */
export function SansOperateur({
  titre,
  chargement,
}: {
  titre: string
  chargement: boolean
}) {
  return (
    <PageMetier
      titre={titre}
      description={
        chargement
          ? 'Chargement…'
          : "Aucun opérateur de démonstration : lancez l'application avec le profil demo."
      }
    >
      {null}
    </PageMetier>
  )
}
