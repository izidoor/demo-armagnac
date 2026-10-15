import { useState } from 'react'
import { ChevronDown } from 'lucide-react'
import { cn } from '@/lib/utils'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'
import { OngletAnnuaire } from '../personnes/annuaire'
import { OngletJournal } from '../personnes/journal'

/**
 * « Mes utilisateurs » du portail (spec 02, §6.2, menu ex-« Annuaire ») : les personnes de l'opérateur actif
 * (RG-OPE-52), puis le journal des actions (RG-OPE-34) dans une carte repliée, chargé à l'ouverture ; contacts selon
 * le module Contacts, invitations et accès selon le module Accès.
 */
export function PageAnnuaire() {
  const { actif } = useMoi()
  const operateur = actif?.operateur ?? 0
  const droits = useDroits(operateur)
  const [journalOuvert, setJournalOuvert] = useState(false)
  return (
    <PageMetier
      titre='Mes utilisateurs'
      description={`Les personnes de ${actif?.raisonSociale ?? 'votre opérateur'} : contacts, membres et invitations.`}
    >
      {droits && (
        <div className='mb-8 space-y-8'>
          <OngletAnnuaire
            espace='portail'
            operateur={operateur}
            droits={droits}
          />
          <Collapsible open={journalOuvert} onOpenChange={setJournalOuvert}>
            <Card>
              <CardHeader>
                <CollapsibleTrigger className='flex w-full items-center justify-between gap-2 text-start'>
                  <CardTitle className='text-base'>
                    Journal des accès utilisateurs
                  </CardTitle>
                  <ChevronDown
                    className={cn(
                      'text-muted-foreground size-4 transition-transform',
                      journalOuvert && 'rotate-180'
                    )}
                  />
                </CollapsibleTrigger>
              </CardHeader>
              <CollapsibleContent>
                <CardContent>
                  <OngletJournal operateur={operateur} />
                </CardContent>
              </CollapsibleContent>
            </Card>
          </Collapsible>
        </div>
      )}
    </PageMetier>
  )
}
