import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { ArrowLeft, TriangleAlert } from 'lucide-react'
import { toast } from 'sonner'
import {
  attribuerNouveauCodeOperateur,
  desactiverOperateur,
  reactiverOperateur,
  supprimerOperateur,
} from '@/api/generated'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { BadgeEtat } from '@/components/metier/badge-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { apiOperateur, cleOperateur, type Espace } from '../commun/api'
import { TYPES } from '../commun/libelles'
import { OngletContacts } from './onglet-contacts'
import { OngletDiffusion } from './onglet-diffusion'
import { OngletGeneral } from './onglet-general'
import { OngletProduction } from './onglet-production'
import {
  SectionMarques,
  SectionOenotourisme,
  SectionVisitesLabels,
} from './presentation'
import { usePresentation } from './use-presentation'

type Action = 'desactiver' | 'reactiver' | 'supprimer'

/**
 * Fiche opérateur par onglets (spec 02, §6.1) ; au portail, « Mon entreprise » (§6.2), dans les mêmes
 * onglets, sans le commentaire interne ni les actions du BNIA ; les contacts y ont leur propre page.
 */
export function PageOperateur({ espace, id }: { espace: Espace; id: number }) {
  const bo = espace === 'bo'
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const api = apiOperateur(espace, id)
  const requete = useQuery({
    queryKey: [...cleOperateur(id), espace],
    queryFn: api.fiche,
  })
  const presentation = usePresentation(espace, id)
  const [action, setAction] = useState<Action | null>(null)
  const rafraichir = () => {
    queryClient.invalidateQueries({ queryKey: cleOperateur(id) })
    queryClient.invalidateQueries({ queryKey: ['operateurs'] })
  }

  const nouveauCode = useMutation({
    mutationFn: () =>
      attribuerNouveauCodeOperateur({ path: { id }, throwOnError: true }),
    onSuccess: (r) => {
      toast.success(`Nouveau code : ${r.data.code}.`)
      rafraichir()
    },
  })
  const etat = useMutation({
    mutationFn: async (a: Action) => {
      if (a === 'desactiver')
        await desactiverOperateur({ path: { id }, throwOnError: true })
      else if (a === 'reactiver')
        await reactiverOperateur({ path: { id }, throwOnError: true })
      else await supprimerOperateur({ path: { id }, throwOnError: true })
    },
    onSuccess: (_r, a) => {
      setAction(null)
      if (a === 'supprimer') {
        toast.success('Opérateur supprimé.')
        queryClient.invalidateQueries({ queryKey: ['operateurs'] })
        navigate({ to: '/bo/operateurs' })
      } else rafraichir()
    },
    onError: () => setAction(null),
  })

  const fiche = requete.data
  if (!fiche) {
    return (
      <PageMetier
        titre={bo ? 'Opérateur' : 'Mon entreprise'}
        description={
          requete.isError ? "La fiche n'a pas pu être chargée." : 'Chargement…'
        }
      >
        {null}
      </PageMetier>
    )
  }

  const avecContacts = fiche.contacts.filter((c) => c.acces).length
  return (
    <PageMetier
      titre={
        bo ? fiche.raisonSociale : `Mon entreprise · ${fiche.raisonSociale}`
      }
      description={
        <span className='flex flex-wrap items-center gap-2'>
          {bo && (
            <Button
              variant='link'
              className='h-auto p-0'
              onClick={() => navigate({ to: '/bo/operateurs' })}
            >
              <ArrowLeft className='size-4' /> Opérateurs
            </Button>
          )}
          <span className='font-mono'>{fiche.code}</span>
          <Badge variant='secondary'>{TYPES[fiche.type]}</Badge>
          <BadgeEtat actif={fiche.actif} />
          {fiche.nomCommercial && <span>{fiche.nomCommercial}</span>}
        </span>
      }
      actions={
        bo && (
          <div className='flex gap-2'>
            <Button
              variant='outline'
              onClick={() =>
                setAction(fiche.actif ? 'desactiver' : 'reactiver')
              }
            >
              {fiche.actif ? 'Désactiver' : 'Réactiver'}
            </Button>
            <Button
              variant='destructive'
              onClick={() => setAction('supprimer')}
            >
              Supprimer
            </Button>
          </div>
        )
      }
    >
      <div className='mb-4 space-y-2'>
        {fiche.champsManquants.length > 0 && (
          <Alert>
            <TriangleAlert />
            <AlertTitle>Fiche incomplète</AlertTitle>
            <AlertDescription>
              Il manque, pour facturer ou certifier :{' '}
              {fiche.champsManquants.join(', ')}.
            </AlertDescription>
          </Alert>
        )}
        {bo && !fiche.codeConforme && (
          <Alert>
            <TriangleAlert />
            <AlertTitle>Code non conforme</AlertTitle>
            <AlertDescription className='flex flex-wrap items-center gap-2'>
              Le code {fiche.code} n'a plus la forme de son type ou de son
              département.
              {fiche.codePropose && (
                <Button
                  size='sm'
                  variant='outline'
                  onClick={() => nouveauCode.mutate()}
                >
                  Attribuer un nouveau code ({fiche.codePropose})
                </Button>
              )}
            </AlertDescription>
          </Alert>
        )}
      </div>
      <Tabs defaultValue='general'>
        <TabsList className='flex-wrap'>
          <TabsTrigger value='general'>Général</TabsTrigger>
          <TabsTrigger value='production'>Production</TabsTrigger>
          <TabsTrigger value='diffusion'>Diffusion et statistiques</TabsTrigger>
          <TabsTrigger value='marques'>Marques</TabsTrigger>
          <TabsTrigger value='visites'>Visites et labels</TabsTrigger>
          <TabsTrigger value='oenotourisme'>Œnotourisme</TabsTrigger>
          {bo && (
            <TabsTrigger value='contacts'>
              Contacts ({fiche.contacts.length})
            </TabsTrigger>
          )}
        </TabsList>
        <TabsContent value='general'>
          <OngletGeneral
            espace={espace}
            fiche={fiche}
            onEnregistre={enregistre(rafraichir)}
          />
        </TabsContent>
        <TabsContent value='production'>
          <OngletProduction
            espace={espace}
            fiche={fiche}
            onEnregistre={enregistre(rafraichir)}
          />
        </TabsContent>
        <TabsContent value='diffusion'>
          <OngletDiffusion
            key={`${fiche.niveauDiffusion}-${fiche.millesimesDiffusables.join()}-${fiche.accesStatistiques}`}
            espace={espace}
            fiche={fiche}
            onEnregistre={enregistre(rafraichir)}
          />
        </TabsContent>
        <TabsContent value='marques'>
          <SectionMarques presentation={presentation} />
        </TabsContent>
        <TabsContent value='visites'>
          <SectionVisitesLabels presentation={presentation} />
        </TabsContent>
        <TabsContent value='oenotourisme'>
          <SectionOenotourisme presentation={presentation} />
        </TabsContent>
        <TabsContent value='contacts'>
          <OngletContacts
            espace={espace}
            fiche={fiche}
            onEnregistre={rafraichir}
          />
        </TabsContent>
      </Tabs>
      <ConfirmDialog
        open={action !== null}
        onOpenChange={(o) => !o && setAction(null)}
        title={
          action === 'supprimer'
            ? "Supprimer l'opérateur"
            : action === 'desactiver'
              ? "Désactiver l'opérateur"
              : "Réactiver l'opérateur"
        }
        desc={
          action === 'supprimer'
            ? `${fiche.raisonSociale} sera supprimé avec ses contacts et sa présentation. Si d'autres données le référencent, la suppression sera refusée : désactivez-le plutôt.`
            : action === 'desactiver'
              ? `${fiche.raisonSociale} restera consultable, mais ne sera plus proposé.${avecContacts > 0 ? ` ${avecContacts} contact(s) perdront l'accès au portail.` : ''}`
              : `${fiche.raisonSociale} sera de nouveau proposé. L'accès au portail se réaccorde contact par contact.`
        }
        confirmText={
          action === 'supprimer'
            ? 'Supprimer'
            : action === 'desactiver'
              ? 'Désactiver'
              : 'Réactiver'
        }
        cancelBtnText='Annuler'
        destructive={action !== 'reactiver'}
        isLoading={etat.isPending}
        handleConfirm={() => action && etat.mutate(action)}
      />
    </PageMetier>
  )
}

function enregistre(rafraichir: () => void) {
  return () => {
    toast.success('Enregistré.')
    rafraichir()
  }
}
