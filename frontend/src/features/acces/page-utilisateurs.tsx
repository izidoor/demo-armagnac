import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { ArrowLeft } from 'lucide-react'
import { toast } from 'sonner'
import {
  bloquerUtilisateur,
  consulterUtilisateur,
  debloquerUtilisateur,
  listerEtatsComptes,
  listerUtilisateurs,
  type EtatCompte,
} from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
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
import { CelluleOuvrir, EnteteOuvrir } from '@/components/metier/cellule-ouvrir'
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

/** Utilisateurs, au back-office (RG-OPE-53) : liste filtrable par nom ou e-mail, statut, opérateur. */
export function PageUtilisateurs() {
  const navigate = useNavigate()
  const [recherche, setRecherche] = useState('')
  const [statut, setStatut] = useState(TOUS)
  const texte = useDebounce(recherche.trim())
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
  return (
    <PageMetier
      titre='Utilisateurs'
      description='Les personnes qui ont un compte, et leurs opérateurs.'
    >
      <div className='mb-4 flex flex-wrap gap-2'>
        <Input
          className='max-w-sm'
          placeholder='Nom, prénom ou e-mail'
          value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
        />
        <Select value={statut} onValueChange={setStatut}>
          <SelectTrigger className='w-48'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TOUS}>Tous les statuts</SelectItem>
            <SelectItem value='ACTIF'>Actifs</SelectItem>
            <SelectItem value='BLOQUE'>Bloqués par le BNIA</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nom</TableHead>
              <TableHead>E-mail</TableHead>
              <TableHead>Opérateurs</TableHead>
              <TableHead>État</TableHead>
              <TableHead>Dernière connexion</TableHead>
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
                onClick={() =>
                  navigate({
                    to: '/bo/acces/utilisateurs/$id',
                    params: { id: String(u.id) },
                  })
                }
              >
                <TableCell>
                  {u.prenom} {u.nom}
                </TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell className='text-sm'>
                  {u.rattachements
                    .map((r) => `${r.raisonSociale} (${ROLES[r.role]})`)
                    .join(', ')}
                </TableCell>
                <TableCell>
                  <Etat statut={u.statut} etat={etats?.get(u.id)} />
                </TableCell>
                <TableCell>
                  {etats?.get(u.id)?.derniereConnexion &&
                    dateHeure.format(
                      new Date(etats.get(u.id)!.derniereConnexion!)
                    )}
                </TableCell>
                <CelluleOuvrir
                  libelle={`Ouvrir le compte de ${u.prenom} ${u.nom}`}
                  onOuvrir={() =>
                    navigate({
                      to: '/bo/acces/utilisateurs/$id',
                      params: { id: String(u.id) },
                    })
                  }
                />
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </PageMetier>
  )
}

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
      description={
        <span className='flex flex-wrap items-center gap-2'>
          <Link
            to='/bo/acces/utilisateurs'
            className='inline-flex items-center gap-1 hover:underline'
          >
            <ArrowLeft className='size-4' /> Utilisateurs
          </Link>
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
            <Lecture label='E-mail (identifiant)' valeur={u.email} />
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
          <CardContent>
            <Table>
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
                      <span className='text-muted-foreground'> · {r.code}</span>
                    </TableCell>
                    <TableCell>{ROLES[r.role]}</TableCell>
                    <TableCell>
                      {STATUTS_RATTACHEMENT[r.statut]}
                      {!r.operateurEnActivite && ', opérateur inactif'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
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
      <Dialog open={blocage} onOpenChange={setBlocage}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Bloquer {u.email}</DialogTitle>
            <DialogDescription>
              Ses connexions seront refusées dans tous ses opérateurs, et sa
              session coupée. Il en est prévenu par e-mail, ainsi que les ADMIN
              de ses opérateurs. Le motif reste au journal, sans être envoyé.
            </DialogDescription>
          </DialogHeader>
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
              disabled={action.isPending || !motif.trim()}
              onClick={() => action.mutate(true)}
            >
              Bloquer
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </PageMetier>
  )
}
