import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'
import { OngletAnnuaire } from '../personnes/annuaire'
import { OngletJournal } from '../personnes/journal'

/**
 * « Annuaire » du portail (spec 02, §6.2) : les personnes de l'opérateur actif (RG-OPE-52) et le journal
 * des actions (RG-OPE-34) ; contacts selon le module Contacts, invitations et accès selon le module Accès.
 */
export function PageAnnuaire() {
  const { actif } = useMoi()
  const operateur = actif?.operateur ?? 0
  const droits = useDroits(operateur)
  return (
    <PageMetier
      titre='Annuaire'
      description={`Les personnes de ${actif?.raisonSociale ?? 'votre opérateur'} : contacts, membres et invitations.`}
    >
      {droits && (
        <Tabs defaultValue='annuaire'>
          <TabsList>
            <TabsTrigger value='annuaire'>Personnes</TabsTrigger>
            <TabsTrigger value='journal'>Journal</TabsTrigger>
          </TabsList>
          <TabsContent value='annuaire'>
            <OngletAnnuaire
              espace='portail'
              operateur={operateur}
              droits={droits}
            />
          </TabsContent>
          <TabsContent value='journal'>
            <OngletJournal operateur={operateur} />
          </TabsContent>
        </Tabs>
      )}
    </PageMetier>
  )
}
