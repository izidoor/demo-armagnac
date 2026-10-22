import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { ArrowLeft, Search } from 'lucide-react'
import { toast } from 'sonner'
import {
  bloquerUtilisateur,
  consulterUtilisateur,
  debloquerUtilisateur,
  listerEtatsComptes,
  listerUtilisateurs,
  type EtatCompte,
} from '@/api/generated'
import { cn } from '@/lib/utils'
import { useFiltresAdresse, useTexteAdresse } from '@/hooks/use-filtres-adresse'
import { useRetour, useVersParente } from '@/hooks/use-retour'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Textarea } from '@/components/ui/textarea'
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { CelluleOuvrir, EnteteOuvrir } from '@/components/metier/cellule-ouvrir'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { PageMetier } from '@/components/metier/page-metier'
import { ActionDanger, ZoneDanger } from '@/components/metier/zone-danger'
import { Lecture } from '@/features/operateurs/commun/champs'
import {
  ROLES,
  STATUTS_RATTACHEMENT,
} from '@/features/operateurs/commun/libelles'
import { useDroits, useMoi } from '@/features/session/session'

const dateHeure = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

const TOUS = 'tous'

/** Blocage temporaire et dernière connexion, lus dans `authentification` (RG-OPE-53). */
function useEtatsComptes(utilisateurs: number[]) {
  return useQuery({
    queryKey: ['etats-comptes', utilisateurs],
    queryFn: () =>
      listerEtatsComptes({
        query: { utilisateurs },
        throwOnError: true,
      }).then((r) => new Map(r.data.map((e) => [e.utilisateur, e]))),
    enabled: utilisateurs.length > 0,
  }).data
}

/**
 * Utilisateurs, au back-office (RG-OPE-53) : liste filtrable par nom ou e-mail, statut, opérateur. Les filtres sont
 * gardés dans l'adresse (`useFiltresAdresse`) : le retour depuis un utilisateur les retrouve. Colonnes masquées par
 * paliers et cartes sous un seuil (`SEUILS`).
 */
export function PageUtilisateurs() {
  const navigate = useNavigate()
  const [filtres, changer] = useFiltresAdresse({
    statut: TOUS as string,
    recherche: '',
  })
  const { statut } = filtres
  const [recherche, setRecherche, stable] = useTexteAdresse(
    filtres.recherche,
    (texte) => changer({ recherche: texte })
  )
  const texte = stable.trim()
  const utilisateurs = useQuery({
    queryKey: ['utilisateurs', texte, statut],
    queryFn: () =>
      listerUtilisateurs({
        query: {
          recherche: texte || undefined,
          statut: statut === TOUS ? undefined : (statut as 'ACTIF' | 'BLOQUE'),
        },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const lignes = utilisateurs.data ?? []
  const etats = useEtatsComptes(lignes.map((u) => u.id))
  const ouvrir = (id: number) =>
    navigate({
      to: '/bo/acces/utilisateurs/$id',
      params: { id: String(id) },
    })
  return (
    <PageMetier
      titre='Utilisateurs'
      description='Les personnes qui ont un compte, et leurs opérateurs.'
    >
      <div className='mb-4 flex flex-wrap items-center gap-2'>
        <div className='relative w-full sm:w-auto'>
          <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
          <Input
            placeholder='Rechercher un utilisateur'
            title='Nom, prénom ou e-mail'
            aria-label='Recherche par nom, prénom ou e-mail'
            value={recherche}
            onChange={(e) => setRecherche(e.target.value)}
            className='h-9 w-full ps-8 sm:w-72'
          />
        </div>
        <Select value={statut} onValueChange={(v) => changer({ statut: v })}>
          <SelectTrigger aria-label='Statut' className='h-9 w-48 max-sm:w-full'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TOUS}>Tous les statuts</SelectItem>
            <SelectItem value='ACTIF'>Actifs</SelectItem>
            <SelectItem value='BLOQUE'>Bloqués par le BNIA</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className={cn('rounded-md border', SEUILS.tableau)}>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nom</TableHead>
              <TableHead>E-mail</TableHead>
              <TableHead>Opérateurs</TableHead>
              <TableHead>État</TableHead>
              <TableHead className={SEUILS.connexion}>
                Dernière connexion
              </TableHead>
              <EnteteOuvrir />
            </TableRow>
          </TableHeader>
          <TableBody>
            {lignes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={6}
                  className='text-muted-foreground h-16 text-center'
                >
                  {utilisateurs.isPending
                    ? 'Chargement…'
                    : 'Aucun utilisateur.'}
                </TableCell>
              </TableRow>
            )}
            {lignes.map((u) => (
              <TableRow
                key={u.id}
                className='cursor-pointer'
                onClick={() => ouvrir(u.id)}
              >
                <TableCell>
                  {u.prenom} {u.nom}
                </TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell
                  className='w-full max-w-0 min-w-48 truncate text-sm'
                  title={operateurs(u)}
                >
                  {operateurs(u)}
                </TableCell>
                <TableCell>
                  <Etat statut={u.statut} etat={etats?.get(u.id)} />
                </TableCell>
                <TableCell className={SEUILS.connexion}>
                  {derniereConnexion(etats?.get(u.id))}
                </TableCell>
                <CelluleOuvrir
                  libelle={`Ouvrir le compte de ${u.prenom} ${u.nom}`}
                  onOuvrir={() => ouvrir(u.id)}
                />
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <CartesListe
        requete={utilisateurs}
        vide='Aucun utilisateur.'
        masquee={SEUILS.cartes}
      >
        {lignes.map((u) => (
          <CarteListe
            key={u.id}
            lien={{
              to: '/bo/acces/utilisateurs/$id',
              params: { id: String(u.id) },
            }}
            titre={`${u.prenom} ${u.nom}`}
          >
            <div className='text-muted-foreground truncate text-sm'>
              {u.email}
            </div>
            <div className='line-clamp-2 text-sm' title={operateurs(u)}>
              {operateurs(u)}
            </div>
            <div className='flex flex-wrap items-center gap-2 text-sm'>
              <Etat statut={u.statut} etat={etats?.get(u.id)} />
              {etats?.get(u.id)?.derniereConnexion && (
                <span className='text-muted-foreground'>
                  Connecté le {derniereConnexion(etats.get(u.id))}
                </span>
              )}
            </div>
          </CarteListe>
        ))}
      </CartesListe>
    </PageMetier>
  )
}

/**
 * Selon la largeur du contenu, comme la liste des opérateurs (34 px de marges) : les opérateurs prennent la place qui
 * reste et se tronquent (12rem au moins) ; sous 60rem, le tableau perd la dernière connexion ; sous 49rem, une carte par
 * utilisateur.
 */
const SEUILS = {
  connexion: '@max-[60rem]/content:hidden',
  tableau: '@max-[49rem]/content:hidden',
  cartes: '@min-[49rem]/content:hidden',
}

/** Les opérateurs d'un utilisateur, avec son rôle dans chacun. */
const operateurs = (u: {
  rattachements: { raisonSociale: string; role: keyof typeof ROLES }[]
}) =>
  u.rattachements.map((r) => `${r.raisonSociale} (${ROLES[r.role]})`).join(', ')

const derniereConnexion = (etat?: EtatCompte) =>
  etat?.derniereConnexion
    ? dateHeure.format(new Date(etat.derniereConnexion))
    : undefined

function Etat({ statut, etat }: { statut: string; etat?: EtatCompte }) {
  const bloqueJusqua = etat?.bloqueJusqua
  return (
    <span className='flex flex-wrap gap-1'>
      {statut === 'BLOQUE' ? (
        <Badge variant='destructive'>Bloqué par le BNIA</Badge>
      ) : (
        <Badge variant='outline'>Actif</Badge>
      )}
      {bloqueJusqua && new Date(bloqueJusqua) > new Date() && (
        <Badge variant='secondary'>
          Bloqué jusqu'à {dateHeure.format(new Date(bloqueJusqua))}
        </Badge>
      )}
      {etat && !etat.motDePasseDefini && (
        <Badge variant='secondary'>Mot de passe à définir</Badge>
      )}
    </span>
  )
}

/** Un utilisateur : rattachements, état ; blocage avec motif et déblocage par un ADMIN du BNIA (RG-OPE-50). */
export function PageUtilisateur({ id }: { id: number }) {
  const queryClient = useQueryClient()
  const { actif } = useMoi()
  const droits = useDroits(actif?.operateur ?? 0)
  const [blocage, setBlocage] = useState(false)
  const [motif, setMotif] = useState('')
  const utilisateur = useQuery({
    queryKey: ['utilisateurs', id],
    queryFn: () =>
      consulterUtilisateur({ path: { id }, throwOnError: true }).then(
        (r) => r.data
      ),
  })
  const etats = useEtatsComptes([id])
  const retour = useRetour({ to: '/bo/acces/utilisateurs' })
  const versLaListe = useVersParente({ to: '/bo/acces/utilisateurs' })
  const action = useMutation({
    mutationFn: (bloquer: boolean) =>
      (bloquer
        ? bloquerUtilisateur({
            path: { id },
            body: { motif: motif.trim() },
            throwOnError: true,
          })
        : debloquerUtilisateur({ path: { id }, throwOnError: true })
      ).then((r) => r.data),
    onSuccess: (u) => {
      queryClient.setQueryData(['utilisateurs', id], u)
      queryClient.invalidateQueries({ queryKey: ['utilisateurs'] })
      setBlocage(false)
      setMotif('')
      toast.success(
        u.statut === 'BLOQUE' ? 'Utilisateur bloqué.' : 'Utilisateur débloqué.'
      )
    },
  })
  const u = utilisateur.data
  if (!u) {
    return (
      <PageMetier titre='Utilisateur' description='Chargement…'>
        {null}
      </PageMetier>
    )
  }
  const etat = etats?.get(id)
  return (
    <PageMetier
      titre={`${u.prenom} ${u.nom}`}
      retour={{ libelle: 'Retour', onRetour: retour }}
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Button
            variant='link'
            className='h-auto p-0 max-md:hidden'
            onClick={versLaListe}
          >
            <ArrowLeft className='size-4' /> Utilisateurs
          </Button>
          <Etat statut={u.statut} etat={etat} />
        </span>
      }
    >
      <div className='space-y-4'>
        <Card>
          <CardHeader>
            <CardTitle>Compte</CardTitle>
          </CardHeader>
          <CardContent className='grid gap-4 @min-[48rem]/content:grid-cols-3'>
            <Lecture label='E-mail' valeur={u.email} />
            <Lecture
              label='Dernière connexion'
              valeur={
                etat?.derniereConnexion &&
                dateHeure.format(new Date(etat.derniereConnexion))
              }
            />
            <Lecture
              label='Mot de passe'
              valeur={etat?.motDePasseDefini ? 'Défini' : 'À définir'}
            />
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Opérateurs</CardTitle>
          </CardHeader>
          {/* Sous 32rem de carte, un opérateur par bloc : raison sociale et code, puis rôle et accès. */}
          <CardContent className='@container/rattachements'>
            <Table className='@max-[32rem]/rattachements:hidden'>
              <TableHeader>
                <TableRow>
                  <TableHead>Opérateur</TableHead>
                  <TableHead>Rôle</TableHead>
                  <TableHead>Accès</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {u.rattachements.map((r) => (
                  <TableRow key={r.id}>
                    <TableCell>
                      <LienRattachement rattachement={r} />
                      <span className='text-muted-foreground'> · {r.code}</span>
                    </TableCell>
                    <TableCell>{ROLES[r.role]}</TableCell>
                    <TableCell>{acces(r)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <ul className='divide-y text-sm @min-[32rem]/rattachements:hidden'>
              {u.rattachements.map((r) => (
                <li key={r.id} className='space-y-0.5 py-2 first:pt-0'>
                  <div>
                    <LienRattachement rattachement={r} />
                    <span className='text-muted-foreground'> · {r.code}</span>
                  </div>
                  <div className='text-muted-foreground'>
                    {ROLES[r.role]} · {acces(r)}
                  </div>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>
      {droits?.adminBnia && (
        <ZoneDanger>
          {u.statut === 'BLOQUE' ? (
            <ActionDanger
              titre="Débloquer l'utilisateur"
              description='Ses connexions sont de nouveau acceptées, dans tous ses opérateurs.'
            >
              <Button
                variant='outline'
                disabled={action.isPending}
                onClick={() => action.mutate(false)}
              >
                Débloquer
              </Button>
            </ActionDanger>
          ) : (
            <ActionDanger
              titre="Bloquer l'utilisateur"
              description='Ses connexions sont refusées dans tous ses opérateurs et sa session est coupée ; il en est prévenu par e-mail, ainsi que les ADMIN de ses opérateurs. Il se débloque à tout moment.'
            >
              <Button variant='destructive' onClick={() => setBlocage(true)}>
                Bloquer
              </Button>
            </ActionDanger>
          )}
        </ZoneDanger>
      )}
      <DialogueFormulaire
        ouvert={blocage}
        onOuvertChange={setBlocage}
        titre={`Bloquer ${u.email}`}
        description='Ses connexions seront refusées dans tous ses opérateurs, et sa session coupée. Il en est prévenu par e-mail, ainsi que les ADMIN de ses opérateurs. Le motif reste au journal, sans être envoyé.'
      >
        <div className='space-y-2'>
          <Label htmlFor='motif'>Motif</Label>
          <Textarea
            id='motif'
            value={motif}
            onChange={(e) => setMotif(e.target.value)}
          />
        </div>
        <div className='flex justify-end'>
          <Button
            variant='destructive'
            className='max-md:w-full'
            disabled={action.isPending || !motif.trim()}
            onClick={() => action.mutate(true)}
          >
            Bloquer
          </Button>
        </div>
      </DialogueFormulaire>
    </PageMetier>
  )
}

type Rattachement = NonNullable<
  Awaited<ReturnType<typeof consulterUtilisateur>>['data']
>['rattachements'][number]

/** La fiche du membre chez cet opérateur. */
function LienRattachement({ rattachement: r }: { rattachement: Rattachement }) {
  return (
    <Link
      to='/bo/operateurs/$id/personnes/$genre/$personne'
      params={{
        id: String(r.operateur),
        genre: 'membre',
        personne: String(r.id),
      }}
      className='hover:underline'
    >
      {r.raisonSociale}
    </Link>
  )
}

const acces = (r: Rattachement) =>
  `${STATUTS_RATTACHEMENT[r.statut]}${r.operateurEnActivite ? '' : ', opérateur inactif'}`
