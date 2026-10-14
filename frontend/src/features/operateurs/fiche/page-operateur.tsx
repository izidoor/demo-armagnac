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
import { OngletDeclarations } from '@/features/declarations/onglet-declarations'
import { apiOperateur, cleOperateur, type Espace } from '../commun/api'
import { TYPES } from '../commun/libelles'
import { OngletAnnuaire } from '../personnes/annuaire'
import { OngletJournal } from '../personnes/journal'
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
 * Fiche opérateur par onglets (spec 02, §6.1), ouverte sans changer d'opérateur actif ; au portail,
 * « Mon entreprise » (§6.2), dans les mêmes onglets, l'annuaire ayant son propre menu. Pour l'opérateur
 * BNIA, une fiche réduite : identité, coordonnées et annuaire (RG-OPE-39). Ce que l'on peut faire suit
 * les droits renvoyés avec la fiche (RG-OPE-31, 32, 36). Au back-office, un producteur ou un négociant a
 * l'onglet « Déclarations », fourni par {@code declarations} (spec 05 §6.1).
 */
export function PageOperateur({
  espace,
  id,
  onglet = 'general',
}: {
  espace: Espace
  id: number
  onglet?: string
}) {
  const bo = espace === 'bo'
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const requete = useQuery({
    queryKey: [...cleOperateur(id), 'fiche'],
    queryFn: apiOperateur(id).fiche,
  })
  const presentation = usePresentation(id)
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

  const bnia = fiche.type === 'BNIA'
  const droits = fiche.droits
  const avecAnnuaire = bo
  const avecDeclarations =
    bo && (fiche.type === 'PRODUCTEUR' || fiche.type === 'NEGOCIANT')
  return (
    <PageMetier
      titre={
        bo ? fiche.raisonSociale : `Mon entreprise · ${fiche.raisonSociale}`
      }
      description={
        <span className='flex flex-wrap items-center gap-2'>
          {bo && !bnia && (
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
        droits.adminBnia &&
        !bnia && (
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
        {!bnia && fiche.manquePourFacturer.length > 0 && (
          <Alert>
            <TriangleAlert />
            <AlertTitle>Non facturable</AlertTitle>
            <AlertDescription>
              Il manque : {fiche.manquePourFacturer.join(', ')}.
            </AlertDescription>
          </Alert>
        )}
        {!bnia && fiche.manquePourCertifier.length > 0 && (
          <Alert>
            <TriangleAlert />
            <AlertTitle>Non certifiable</AlertTitle>
            <AlertDescription>
              Il manque : {fiche.manquePourCertifier.join(', ')}.
            </AlertDescription>
          </Alert>
        )}
        {droits.personnelBnia && !fiche.codeConforme && (
          <Alert>
            <TriangleAlert />
            <AlertTitle>Code non conforme</AlertTitle>
            <AlertDescription className='flex flex-wrap items-center gap-2'>
              Le code {fiche.code} n'a plus la forme de son type ou de son
              département.
              {fiche.codePropose && droits.contacts === 'GESTION' && (
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
      <Tabs defaultValue={onglet}>
        <TabsList className='flex-wrap'>
          <TabsTrigger value='general'>
            {bnia ? 'Fiche' : 'Général'}
          </TabsTrigger>
          {!bnia && (
            <>
              <TabsTrigger value='production'>Production</TabsTrigger>
              <TabsTrigger value='diffusion'>
                Diffusion et statistiques
              </TabsTrigger>
              <TabsTrigger value='marques'>Marques</TabsTrigger>
              <TabsTrigger value='visites'>Visites et labels</TabsTrigger>
              <TabsTrigger value='oenotourisme'>Œnotourisme</TabsTrigger>
            </>
          )}
          {avecDeclarations && (
            <TabsTrigger value='declarations'>Déclarations</TabsTrigger>
          )}
          {avecAnnuaire && (
            <>
              <TabsTrigger value='annuaire'>Annuaire</TabsTrigger>
              <TabsTrigger value='journal'>Journal</TabsTrigger>
            </>
          )}
        </TabsList>
        <TabsContent value='general'>
          <OngletGeneral
            espace={espace}
            fiche={fiche}
            onEnregistre={enregistre(rafraichir)}
          />
        </TabsContent>
        {!bnia && (
          <>
            <TabsContent value='production'>
              <OngletProduction
                fiche={fiche}
                onEnregistre={enregistre(rafraichir)}
              />
            </TabsContent>
            <TabsContent value='diffusion'>
              <OngletDiffusion
                key={`${fiche.version}`}
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
          </>
        )}
        {avecDeclarations && (
          <TabsContent value='declarations'>
            <OngletDeclarations
              operateur={id}
              producteur={fiche.type === 'PRODUCTEUR'}
              droits={droits}
            />
          </TabsContent>
        )}
        {avecAnnuaire && (
          <>
            <TabsContent value='annuaire'>
              <OngletAnnuaire espace={espace} operateur={id} droits={droits} />
            </TabsContent>
            <TabsContent value='journal'>
              <OngletJournal operateur={id} />
            </TabsContent>
          </>
        )}
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
            ? `${fiche.raisonSociale} sera supprimé avec sa présentation, ses contacts, ses fiches professionnelles, ses rattachements et ses invitations ; un membre qui n'a pas d'autre opérateur perd son compte. Si d'autres données le référencent, la suppression sera refusée : désactivez-le plutôt.`
            : action === 'desactiver'
              ? `${fiche.raisonSociale} restera consultable, mais ne sera plus proposé. Ses membres n'y auront plus accès (leurs rattachements sont gelés) et ses invitations en attente seront annulées.`
              : `${fiche.raisonSociale} sera de nouveau proposé, et ses membres retrouveront leur accès.`
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
