import { useState, type FormEvent, type ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import {
  Building2,
  History,
  KeyRound,
  LogIn,
  LogOut,
  type LucideIcon,
  ShieldAlert,
  ShieldCheck,
  UserRound,
} from 'lucide-react'
import { toast } from 'sonner'
import {
  changerMotDePasse,
  consulterMembre,
  demanderChangementEmail,
  type LigneConnexion,
  listerMesConnexions,
  modifierFiche,
  modifierMonProfil,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { cn } from '@/lib/utils'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  PageSections,
  type SectionPage,
} from '@/components/layout/page-sections'
import { CadreEditable } from '@/components/metier/cadre-editable'
import { ActionDanger, ZoneDanger } from '@/components/metier/zone-danger'
import { PasswordInput } from '@/components/password-input'
import { SignOutDialog } from '@/components/sign-out-dialog'
import {
  ChampNouveauMotDePasse,
  LONGUEUR_MOT_DE_PASSE,
} from '@/features/auth/mot-de-passe'
import { NotificationsAppareil } from '@/features/notifications/section-notifications'
import { Lecture } from '@/features/operateurs/commun/champs'
import {
  ROLES,
  STATUTS_RATTACHEMENT,
} from '@/features/operateurs/commun/libelles'
import { useListe } from '@/features/operateurs/commun/referentiels'
import { AUCUN } from '@/features/operateurs/commun/saisie'
import { CarteFiche } from '@/features/operateurs/personnes/formulaires'
import {
  requeteMoi,
  useMoi,
  type Rattachement,
} from '@/features/session/session'
import { useChoisirOperateur } from '@/features/session/use-choisir-operateur'
import { Affichage } from './affichage'

const dateHeure = new Intl.DateTimeFormat('fr-FR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

/**
 * « Mon compte » (spec 02 §6.3, spec 03 §6.1, spec 11 §6), mis en page comme le Paramétrage : profil, affichage et
 * notifications de l'appareil, e-mail, opérateurs et opérateur actif, fiche professionnelle dans chaque opérateur, mot
 * de passe, connexions.
 * Ouvert à tout utilisateur connecté (RG-OPE-33).
 */
const sections: SectionPage[] = [
  {
    cle: 'profil',
    titre: 'Profil',
    icon: UserRound,
    description:
      "Votre nom, tel que les autres utilisateurs le voient, l'affichage de l'application et ses notifications sur cet appareil.",
  },
  {
    cle: 'compte',
    titre: 'Sécurité',
    icon: KeyRound,
    description:
      "L'e-mail et le mot de passe avec lesquels vous vous connectez.",
  },
  {
    cle: 'operateurs',
    titre: 'Mes opérateurs',
    icon: Building2,
    description:
      "Vos rattachements, l'opérateur sur lequel vous travaillez, et votre fiche professionnelle dans chacun.",
  },
  {
    cle: 'connexions',
    titre: 'Mes connexions',
    icon: History,
    description:
      'Le journal de votre compte : connexions, échecs, déconnexions et changements de mot de passe.',
  },
]

export function PageCompte() {
  const [active, setActive] = useState('profil')
  return (
    <PageSections
      titre='Mon compte'
      sections={sections}
      active={active}
      onChoisir={setActive}
      onglets
      actions={
        <span className='max-md:hidden'>
          <Deconnexion />
        </span>
      }
    >
      {active === 'profil' && (
        <>
          <Profil />
          <Separator className='my-8' />
          <Section titre='Affichage'>
            <Affichage />
          </Section>
          <Separator className='my-8' />
          <Section titre='Notifications sur cet appareil'>
            <NotificationsAppareil />
          </Section>
        </>
      )}
      {active === 'compte' && (
        <>
          <Email />
          <Separator className='my-8' />
          <MotDePasse />
        </>
      )}
      {active === 'operateurs' && <MesOperateurs />}
      {active === 'connexions' && <MesConnexions />}
      {active === 'compte' && (
        <ZoneDanger titre='Session' className='md:hidden'>
          <ActionDanger
            titre='Se déconnecter'
            description='Fermer la session sur cet appareil ; il faudra vous reconnecter pour revenir.'
          >
            <Deconnexion />
          </ActionDanger>
        </ZoneDanger>
      )}
    </PageSections>
  )
}

/**
 * Se déconnecter, après confirmation : en haut à droite sur écran large, où le menu du profil le propose aussi ; sur
 * mobile, dont l'en-tête n'a plus ce menu, en bas de la section « Sécurité », dans un cadre « Session ».
 */
function Deconnexion() {
  const [ouvert, setOuvert] = useState(false)
  return (
    <>
      <Button variant='outline' onClick={() => setOuvert(true)}>
        <LogOut /> Se déconnecter
      </Button>
      <SignOutDialog open={ouvert} onOpenChange={setOuvert} />
    </>
  )
}

/** Une sous-section, titrée ou non, sous l'en-tête de la section. */
function Section({ titre, children }: { titre?: string; children: ReactNode }) {
  return (
    <section className='space-y-4'>
      {titre && <h4 className='text-base font-semibold'>{titre}</h4>}
      {children}
    </section>
  )
}

function Erreur({ erreur }: { erreur: unknown }) {
  if (!erreur) return null
  const p = lireProbleme(erreur)
  return (
    <Alert variant='destructive'>
      <AlertDescription>{p.detail ?? p.titre}</AlertDescription>
    </Alert>
  )
}

/** Titre, nom et prénom, modifiés par la personne seule ; visibles aussitôt partout (RG-OPE-47). En lecture, puis « Éditer ». */
function Profil() {
  const { moi } = useMoi()
  const queryClient = useQueryClient()
  const titres = useListe('titres')
  const [edition, setEdition] = useState(false)
  const [titre, setTitre] = useState(moi.titre ? String(moi.titre) : AUCUN)
  const [nom, setNom] = useState(moi.nom)
  const [prenom, setPrenom] = useState(moi.prenom)
  const profil = useMutation({
    mutationFn: () =>
      modifierMonProfil({
        body: {
          titre: titre === AUCUN ? undefined : Number(titre),
          nom: nom.trim(),
          prenom: prenom.trim(),
          version: moi.version,
        },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: (r) => {
      queryClient.setQueryData(requeteMoi.queryKey, r.data)
      toast.success('Profil enregistré.')
      setEdition(false)
    },
  })
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    profil.mutate()
  }
  const annuler = () => {
    setTitre(moi.titre ? String(moi.titre) : AUCUN)
    setNom(moi.nom)
    setPrenom(moi.prenom)
    profil.reset()
    setEdition(false)
  }
  return (
    <form onSubmit={soumettre}>
      <CadreEditable
        titre='Profil'
        edition={edition}
        modifiable
        onEditer={() => setEdition(true)}
        onAnnuler={annuler}
        enregistrement={profil.isPending}
      >
        {edition ? (
          <div className='space-y-4'>
            <Erreur erreur={profil.error} />
            <div className='grid gap-4 @min-[48rem]/content:grid-cols-3'>
              <div className='space-y-2'>
                <Label>Titre</Label>
                <Select value={titre} onValueChange={setTitre}>
                  <SelectTrigger className='w-full'>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={AUCUN}>—</SelectItem>
                    {titres.proposees([moi.titre]).map((t) => (
                      <SelectItem key={t.id} value={String(t.id)}>
                        {t.libelle}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className='space-y-2'>
                <Label htmlFor='prenom'>Prénom</Label>
                <Input
                  id='prenom'
                  value={prenom}
                  onChange={(e) => setPrenom(e.target.value)}
                />
              </div>
              <div className='space-y-2'>
                <Label htmlFor='nom'>Nom</Label>
                <Input
                  id='nom'
                  value={nom}
                  onChange={(e) => setNom(e.target.value)}
                />
              </div>
            </div>
          </div>
        ) : (
          <div className='grid gap-4 @min-[48rem]/content:grid-cols-3'>
            <Lecture label='Titre' valeur={titres.libelle(moi.titre)} />
            <Lecture label='Prénom' valeur={moi.prenom} />
            <Lecture label='Nom' valeur={moi.nom} />
          </div>
        )}
      </CadreEditable>
    </form>
  )
}

/** Changer d'e-mail : un lien valable 24 heures part au nouvel e-mail (RG-OPE-48). */
function Email() {
  const { moi } = useMoi()
  const queryClient = useQueryClient()
  const [email, setEmail] = useState('')
  const demande = useMutation({
    mutationFn: () =>
      demanderChangementEmail({
        body: { email: email.trim() },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: (r) => {
      queryClient.setQueryData(requeteMoi.queryKey, r.data)
      setEmail('')
    },
  })
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    demande.mutate()
  }
  return (
    <Section titre='E-mail de connexion'>
      <p className='text-sm'>
        Votre e-mail, <strong>{moi.email}</strong>, est aussi votre identifiant
        de connexion.
      </p>
      {moi.nouvelEmail && (
        <Alert>
          <AlertDescription>
            Un lien de confirmation a été envoyé à {moi.nouvelEmail}
            {moi.nouvelEmailExpireLe &&
              `, valable jusqu'au ${dateHeure.format(new Date(moi.nouvelEmailExpireLe))}`}
            . Tant qu'il n'est pas ouvert, rien ne change.
          </AlertDescription>
        </Alert>
      )}
      <Erreur erreur={demande.error} />
      <form onSubmit={soumettre} className='flex flex-wrap items-end gap-2'>
        <div className='min-w-64 flex-1 space-y-2'>
          <Label htmlFor='nouvel-email'>Nouvel e-mail</Label>
          <Input
            id='nouvel-email'
            type='email'
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <Button type='submit' disabled={demande.isPending || !email.trim()}>
          Envoyer le lien de confirmation
        </Button>
      </form>
    </Section>
  )
}

/**
 * Rattachements, rôle, statut ; opérateur actif ; fiche professionnelle dans chacun (RG-OPE-33, 49). Un opérateur par
 * petite carte : l'actif marqué, qui ouvre sa fiche (le BNIA dans le Paramétrage, un autre « Mon entreprise ») ; les
 * autres utilisables avec « Travailler ici », seule porte vers leur espace.
 */
function MesOperateurs() {
  const { moi, actif } = useMoi()
  const choix = useChoisirOperateur()
  return (
    <div className='space-y-8'>
      <ul className='grid gap-3 @min-[40rem]/content:grid-cols-2 @min-[80rem]/content:grid-cols-3'>
        {moi.rattachements.map((r) => {
          const estActif = r.operateur === actif?.operateur
          return (
            <li
              key={r.id}
              className={cn(
                'bg-card relative flex flex-col gap-2 rounded-lg border p-3 shadow-xs',
                estActif && 'border-primary hover:bg-muted/40 transition-colors'
              )}
            >
              <div className='flex items-start justify-between gap-2'>
                <div className='min-w-0'>
                  {estActif ? (
                    <Link
                      to={
                        r.type === 'BNIA'
                          ? '/bo/parametrage/bnia'
                          : '/portail/entreprise'
                      }
                      className='font-medium after:absolute after:inset-0 after:rounded-lg hover:underline'
                    >
                      {r.raisonSociale}
                    </Link>
                  ) : (
                    <p className='font-medium'>{r.raisonSociale}</p>
                  )}
                  <p className='text-muted-foreground font-mono text-sm'>
                    {r.code}
                  </p>
                </div>
                {estActif && <Badge className='shrink-0'>Actif</Badge>}
              </div>
              <div className='flex flex-wrap gap-1'>
                <Badge variant='outline'>{ROLES[r.role]}</Badge>
                {r.statut !== 'ACTIF' && (
                  <Badge variant='secondary'>
                    {STATUTS_RATTACHEMENT[r.statut]}
                  </Badge>
                )}
                {!r.operateurEnActivite && (
                  <Badge variant='secondary'>Opérateur inactif</Badge>
                )}
              </div>
              {!estActif && r.utilisable && (
                <Button
                  size='sm'
                  variant='outline'
                  className='mt-auto self-start'
                  disabled={choix.isPending}
                  onClick={() => choix.mutate(r.operateur)}
                >
                  Travailler ici
                </Button>
              )}
            </li>
          )
        })}
      </ul>
      {moi.rattachements
        .filter((r) => r.statut === 'ACTIF' && r.operateurEnActivite)
        .map((r) => (
          <MaFiche key={r.id} rattachement={r} />
        ))}
    </div>
  )
}

/** Sa fiche professionnelle dans un opérateur, modifiable par son titulaire (RG-OPE-33, 46). */
function MaFiche({ rattachement }: { rattachement: Rattachement }) {
  const membre = useQuery({
    queryKey: ['operateur', rattachement.operateur, 'membre', rattachement.id],
    queryFn: () =>
      consulterMembre({
        path: { operateur: rattachement.operateur, membre: rattachement.id },
        throwOnError: true,
      }).then((r) => r.data),
  })
  const m = membre.data
  if (!m) return null
  return (
    <CarteFiche
      titre={`Ma fiche chez ${rattachement.raisonSociale}`}
      fiche={m.fiche}
      modifiable
      enregistrer={(body) =>
        modifierFiche({
          path: { operateur: rattachement.operateur, fiche: m.fiche.id },
          body,
          throwOnError: true,
        })
      }
      onSucces={() => {
        toast.success('Fiche enregistrée.')
        membre.refetch()
      }}
    />
  )
}

/** Changer son mot de passe, l'actuel exigé (RG-AUT-06, 07). */
function MotDePasse() {
  const [actuel, setActuel] = useState('')
  const [nouveau, setNouveau] = useState('')
  const changement = useMutation({
    mutationFn: () =>
      changerMotDePasse({ body: { actuel, nouveau }, throwOnError: true }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setActuel('')
      setNouveau('')
      toast.success(
        'Mot de passe changé. Les navigateurs de confiance devront de nouveau recevoir un code.'
      )
    },
  })
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    changement.mutate()
  }
  return (
    <Section titre='Mot de passe'>
      <Erreur erreur={changement.error} />
      <form onSubmit={soumettre} className='max-w-md space-y-4'>
        <div className='space-y-2'>
          <Label htmlFor='actuel'>Mot de passe actuel</Label>
          <PasswordInput
            id='actuel'
            autoComplete='current-password'
            value={actuel}
            onChange={(e) => setActuel(e.target.value)}
          />
        </div>
        <ChampNouveauMotDePasse valeur={nouveau} onChange={setNouveau} />
        <Button
          type='submit'
          disabled={
            changement.isPending ||
            !actuel ||
            nouveau.length < LONGUEUR_MOT_DE_PASSE
          }
        >
          Changer le mot de passe
        </Button>
      </form>
    </Section>
  )
}

/** Ce que dit un événement du journal : un échec ou un refus ressort en rouge, l'icône doublant la couleur. */
function iconeConnexion(evenement: LigneConnexion['evenement']): {
  Icone: LucideIcon
  alerte?: boolean
} {
  switch (evenement) {
    case 'CONNEXION_REUSSIE':
      return { Icone: LogIn }
    case 'DECONNEXION':
      return { Icone: LogOut }
    case 'ECHEC_MOT_DE_PASSE':
    case 'ECHEC_CODE':
    case 'BLOCAGE_DECLENCHE':
    case 'CONNEXION_REFUSEE':
      return { Icone: ShieldAlert, alerte: true }
    case 'NAVIGATEUR_DE_CONFIANCE':
    case 'DOUBLE_AUTHENTIFICATION_ACTIVEE':
    case 'DOUBLE_AUTHENTIFICATION_DESACTIVEE':
      return { Icone: ShieldCheck }
    default:
      return { Icone: KeyRound }
  }
}

/**
 * Ses propres connexions (RG-AUT-13), le plus récent d'abord : un journal, donc un tableau ; une petite carte par
 * événement seulement quand le tableau ne tient pas (seuil de conteneur, voir `CartesListe`). Le tableau tient en 630 px
 * environ (date, événement le plus long, adresse IPv6, navigateur tronqué à ce qui reste), à côté du menu des sections :
 * 332 px de plus : 58rem de contenu, le tableau défilant dans son cadre pour un libellé long sur une adresse IPv6.
 */
function MesConnexions() {
  const connexions = useQuery({
    queryKey: ['mes-connexions'],
    queryFn: () =>
      listerMesConnexions({ throwOnError: true }).then((r) => r.data),
  })
  const lignes = connexions.data ?? []
  if (connexions.isPending || lignes.length === 0)
    return (
      <p className='text-muted-foreground rounded-md border p-4 text-center text-sm'>
        {connexions.isPending ? 'Chargement…' : 'Aucun événement.'}
      </p>
    )
  return (
    <>
      <div className='rounded-md border @max-[58rem]/content:hidden'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Date</TableHead>
              <TableHead>Événement</TableHead>
              <TableHead>Adresse IP</TableHead>
              <TableHead>Navigateur</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {lignes.map((l, i) => (
              <TableRow key={i}>
                <TableCell className='whitespace-nowrap'>
                  {dateHeure.format(new Date(l.date))}
                </TableCell>
                <TableCell className='whitespace-nowrap'>{l.libelle}</TableCell>
                <TableCell className='whitespace-nowrap'>
                  {l.adresseIp}
                </TableCell>
                <TableCell
                  className='w-full max-w-0 min-w-24 truncate'
                  title={l.navigateur}
                >
                  {l.navigateur}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <ul className='grid gap-3 @min-[40rem]/content:grid-cols-2 @min-[58rem]/content:hidden'>
        {lignes.map((l, i) => {
          const { Icone, alerte } = iconeConnexion(l.evenement)
          return (
            <li
              key={i}
              className='bg-card space-y-1 rounded-lg border p-3 shadow-xs'
            >
              <p
                className={cn(
                  'flex items-start gap-2 font-medium',
                  alerte && 'text-destructive'
                )}
              >
                <Icone className='mt-0.5 size-4 shrink-0' aria-hidden='true' />
                {l.libelle}
              </p>
              <p className='text-muted-foreground text-sm'>
                {dateHeure.format(new Date(l.date))}
                {l.adresseIp && ` · ${l.adresseIp}`}
              </p>
              {l.navigateur && (
                <p
                  className='text-muted-foreground line-clamp-2 text-xs break-all'
                  title={l.navigateur}
                >
                  {l.navigateur}
                </p>
              )}
            </li>
          )
        })}
      </ul>
    </>
  )
}
