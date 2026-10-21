import { type ReactNode, useState } from 'react'
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
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { BadgeEtat } from '@/components/metier/badge-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { ActionDanger, ZoneDanger } from '@/components/metier/zone-danger'
import { OngletDeclarations } from '@/features/declarations/onglet-declarations'
import { OngletFactures } from '@/features/facturation/onglet-factures'
import { apiOperateur, cleOperateur, type Espace } from '../commun/api'
import { TYPES } from '../commun/libelles'
import { OngletAnnuaire } from '../personnes/annuaire'
import { OngletDiffusion } from './onglet-diffusion'
import { OngletGeneral } from './onglet-general'
import { OngletProduction } from './onglet-production'
import { SectionOenotourisme } from './presentation'
import { usePresentation } from './use-presentation'

type Action = 'desactiver' | 'reactiver' | 'supprimer'

/**
 * Fiche opérateur par onglets (spec 02, §6.1), ouverte sans changer d'opérateur actif ; au portail,
 * « Mon entreprise » (§6.2), dans les mêmes onglets, l'annuaire ayant son propre menu. La fiche réduite de
 * l'opérateur BNIA (RG-OPE-39) est dans les paramètres de l'application (`SectionBnia`). Ce que l'on peut faire suit
 * les droits renvoyés avec la fiche (RG-OPE-31, 32, 36). Au back-office, un producteur ou un négociant a
 * l'onglet « Stocks & Mouvements », fourni par {@code declarations} (spec 05 §6.1).
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
    >
      <div className='my-6 space-y-3 empty:hidden'>
        {fiche.manquePourFacturer.length > 0 && (
          <Alert variant='warning'>
            <TriangleAlert />
            <AlertTitle>Impossible de facturer</AlertTitle>
            <AlertDescription>
              La fiche de l'opérateur n'est pas correctement renseignée. Il
              manque : {fiche.manquePourFacturer.join(', ')}.
            </AlertDescription>
          </Alert>
        )}
        {fiche.manquePourCertifier.length > 0 && (
          <Alert variant='warning'>
            <TriangleAlert />
            <AlertTitle>Non certifiable</AlertTitle>
            <AlertDescription>
              Il manque : {fiche.manquePourCertifier.join(', ')}.
            </AlertDescription>
          </Alert>
        )}
        {droits.personnelBnia && !fiche.codeConforme && (
          <Alert variant='warning'>
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
        {/* Au back-office, deux groupes d'onglets : la fiche, qui décrit l'opérateur, et son activité. Au portail,
            la fiche seule, sans libellé : l'activité a ses propres menus (déclarations, factures). */}
        <div className='flex flex-wrap items-end gap-x-8 gap-y-4'>
          <GroupeOnglets libelle={bo ? "Fiche de l'opérateur" : undefined}>
            <TabsTrigger value='general'>Général</TabsTrigger>
            {avecAnnuaire && (
              <TabsTrigger value='annuaire'>Contacts</TabsTrigger>
            )}
            <TabsTrigger value='production'>Production</TabsTrigger>
            <TabsTrigger value='oenotourisme'>Œnotourisme</TabsTrigger>
            <TabsTrigger value='diffusion'>Statistiques</TabsTrigger>
          </GroupeOnglets>
          {bo && (
            <GroupeOnglets libelle='Suivi des activités'>
              {avecDeclarations && (
                <TabsTrigger value='declarations'>
                  Stocks & Mouvements
                </TabsTrigger>
              )}
              <TabsTrigger value='contrats'>Contrats</TabsTrigger>
              <TabsTrigger value='exportations'>Exportations</TabsTrigger>
              <TabsTrigger value='factures'>Factures</TabsTrigger>
            </GroupeOnglets>
          )}
        </div>
        <TabsContent value='general'>
          <OngletGeneral
            espace={espace}
            fiche={fiche}
            onEnregistre={enregistre(rafraichir)}
          />
        </TabsContent>
        <TabsContent value='production'>
          <OngletProduction
            fiche={fiche}
            presentation={presentation}
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
        <TabsContent value='oenotourisme'>
          <SectionOenotourisme
            presentation={presentation}
            modifiable={droits.contacts === 'GESTION'}
          />
        </TabsContent>
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
          <TabsContent value='annuaire'>
            <OngletAnnuaire
              espace={espace}
              operateur={id}
              droits={droits}
              avecJournal
            />
          </TabsContent>
        )}
        <TabsContent value='contrats'>
          <AVenir titre='Contrats' />
        </TabsContent>
        <TabsContent value='exportations'>
          <AVenir titre='Exportations' />
        </TabsContent>
        {bo && (
          <TabsContent value='factures'>
            <OngletFactures operateur={id} />
          </TabsContent>
        )}
      </Tabs>
      {droits.adminBnia && (
        <ZoneDanger>
          {fiche.actif ? (
            <ActionDanger
              titre="Désactiver l'opérateur"
              description="Il reste consultable, mais n'est plus proposé ; ses membres n'y ont plus accès et ses invitations en attente sont annulées. Il se réactive à tout moment."
            >
              <Button
                variant='outline'
                className='text-destructive'
                onClick={() => setAction('desactiver')}
              >
                Désactiver
              </Button>
            </ActionDanger>
          ) : (
            <ActionDanger
              titre="Réactiver l'opérateur"
              description='Il est de nouveau proposé, et ses membres retrouvent leur accès.'
            >
              <Button variant='outline' onClick={() => setAction('reactiver')}>
                Réactiver
              </Button>
            </ActionDanger>
          )}
          <ActionDanger
            titre="Supprimer l'opérateur"
            description="Définitivement, avec sa présentation, ses contacts, ses fiches professionnelles, ses rattachements et ses invitations. Refusé si d'autres données le référencent : désactivez-le plutôt."
          >
            <Button
              variant='destructive'
              onClick={() => setAction('supprimer')}
            >
              Supprimer
            </Button>
          </ActionDanger>
        </ZoneDanger>
      )}
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

/** Une barre d'onglets, sous le libellé de son groupe. */
function GroupeOnglets({
  libelle,
  children,
}: {
  libelle?: string
  children: ReactNode
}) {
  return (
    <div className='space-y-1.5'>
      {libelle && (
        <div className='text-muted-foreground text-xs font-medium tracking-wide uppercase'>
          {libelle}
        </div>
      )}
      <TabsList className='flex-wrap'>{children}</TabsList>
    </div>
  )
}

/** Un onglet réservé à un contexte pas encore livré (contrats, suivis de sortie export). */
function AVenir({ titre }: { titre: string }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{titre}</CardTitle>
      </CardHeader>
      <CardContent className='text-muted-foreground'>À venir.</CardContent>
    </Card>
  )
}

function enregistre(rafraichir: () => void) {
  return () => {
    toast.success('Enregistré.')
    rafraichir()
  }
}
