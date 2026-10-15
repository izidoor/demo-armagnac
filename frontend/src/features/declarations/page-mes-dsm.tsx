import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { ChevronDown, Info } from 'lucide-react'
import { toast } from 'sonner'
import { cn } from '@/lib/utils'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'
import {
  apiDeclarations,
  cleDeclarations,
  useDsms,
  usePointDeDepart,
} from './api'
import { BandeauEcheance } from './echeance'
import { ListeDsm } from './liste-dsm'
import { ResumePointDeDepart } from './point-de-depart'

/**
 * « DSM » au portail (spec 05 §6.2) : les DSM de l'opérateur actif, le bouton « Initier la DSM de
 * <période> » ou la raison qui l'empêche (RG-DEC-02 à 05), le point de départ en lecture (carte dépliable « Stock
 * initial ») ; le bandeau de
 * l'échéance et du retard de la période attendue (RG-DEC-39, 40).
 */
export function PageMesDsm() {
  const { actif } = useMoi()
  if (!actif) return null
  return <MesDsm key={actif.operateur} operateur={actif.operateur} />
}

function MesDsm({ operateur }: { operateur: number }) {
  const [stockInitialOuvert, setStockInitialOuvert] = useState(false)
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const api = apiDeclarations(operateur)
  const droits = useDroits(operateur)
  const situation = useQuery({
    queryKey: [...cleDeclarations(operateur), 'situation'],
    queryFn: api.situation,
  })
  const dsms = useDsms(operateur)
  const point = usePointDeDepart(operateur)
  const initiation = useMutation({
    mutationFn: api.initier,
    onSuccess: (dsm) => {
      toast.success(`DSM de ${dsm.periode.libelle} initiée.`)
      queryClient.invalidateQueries({ queryKey: cleDeclarations(operateur) })
      navigate({
        to: '/portail/declarations/$dsm',
        params: { dsm: String(dsm.id) },
      })
    },
  })
  const s = situation.data
  const gere = droits?.declarations === 'GESTION'
  // La période attendue : à initier, ou celle de la DSM en cours ; une DSM envoyée attend le BNIA.
  const enCours = dsms.data?.find((d) => d.id === s?.dsmEnCours)
  const attendue =
    s?.periode && s.echeance && (s.initiable || s.dsmEnCours != null)
      ? { periode: s.periode.libelle, echeance: s.echeance }
      : null

  return (
    <PageMetier
      titre='DSM'
      description='Déclarations de stocks et mouvements de votre entreprise.'
      actions={
        s?.initiable &&
        gere && (
          <Button
            onClick={() => initiation.mutate()}
            disabled={initiation.isPending}
          >
            Initier la DSM de {s.periode?.libelle}
          </Button>
        )
      }
    >
      <div className='space-y-6'>
        {attendue && (
          <BandeauEcheance
            periode={attendue.periode}
            echeance={attendue.echeance}
            enRetard={s?.enRetard ?? false}
            attendBnia={
              enCours != null &&
              ['ENVOYEE', 'EN_INSTRUCTION', 'COMPLEMENTS_RECUS'].includes(
                enCours.etat
              )
            }
          />
        )}
        {s && !s.initiable && (
          <Alert>
            <Info />
            <AlertTitle>
              {s.periode
                ? `Prochaine DSM : ${s.periode.libelle}`
                : 'Aucune DSM à initier'}
            </AlertTitle>
            <AlertDescription>{s.message}</AlertDescription>
          </Alert>
        )}
        <Card>
          <CardHeader>
            <CardTitle className='text-base'>Mes déclarations</CardTitle>
          </CardHeader>
          <CardContent>
            <ListeDsm
              espace='portail'
              operateur={operateur}
              dsms={dsms.data ?? []}
            />
          </CardContent>
        </Card>
        {point.data && (
          <Collapsible
            open={stockInitialOuvert}
            onOpenChange={setStockInitialOuvert}
          >
            <Card>
              <CardHeader>
                <CollapsibleTrigger className='flex w-full items-center justify-between gap-2 text-start'>
                  <CardTitle className='text-base'>Stock initial</CardTitle>
                  <ChevronDown
                    className={cn(
                      'text-muted-foreground size-4 transition-transform',
                      stockInitialOuvert && 'rotate-180'
                    )}
                  />
                </CollapsibleTrigger>
              </CardHeader>
              <CollapsibleContent>
                <CardContent className='space-y-2'>
                  <p className='text-muted-foreground text-sm'>
                    Fixé par le BNIA : la première période commence à cette
                    date, avec ce stock initial.
                  </p>
                  <ResumePointDeDepart point={point.data} />
                </CardContent>
              </CollapsibleContent>
            </Card>
          </Collapsible>
        )}
      </div>
    </PageMetier>
  )
}
