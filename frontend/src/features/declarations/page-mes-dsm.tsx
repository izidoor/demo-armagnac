import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { Info } from 'lucide-react'
import { toast } from 'sonner'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'
import {
  apiDeclarations,
  cleDeclarations,
  useDsms,
  usePointDeDepart,
} from './api'
import { ListeDsm } from './liste-dsm'
import { ResumePointDeDepart } from './point-de-depart'

/**
 * « Mes DSM » au portail (spec 05 §6.2) : les DSM de l'opérateur actif, le bouton « Initier la DSM de
 * <période> » ou la raison qui l'empêche (RG-DEC-02 à 05), le point de départ en lecture.
 */
export function PageMesDsm() {
  const { actif } = useMoi()
  if (!actif) return null
  return <MesDsm key={actif.operateur} operateur={actif.operateur} />
}

function MesDsm({ operateur }: { operateur: number }) {
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

  return (
    <PageMetier
      titre='Mes DSM'
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
        <section className='space-y-2'>
          <h3 className='text-lg font-semibold'>Mes déclarations</h3>
          <ListeDsm
            espace='portail'
            operateur={operateur}
            dsms={dsms.data ?? []}
          />
        </section>
        {point.data && (
          <section className='space-y-2'>
            <h3 className='text-lg font-semibold'>Point de départ</h3>
            <p className='text-muted-foreground text-sm'>
              Fixé par le BNIA : la première période commence à cette date, avec
              ce stock initial.
            </p>
            <ResumePointDeDepart point={point.data} />
          </section>
        )}
      </div>
    </PageMetier>
  )
}
