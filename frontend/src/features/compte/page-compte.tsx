import { Fragment, useState, type FormEvent, type ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Building2, History, KeyRound, UserRound } from 'lucide-react'
import { toast } from 'sonner'
import {
  changerMotDePasse,
  consulterMembre,
  demanderChangementEmail,
  listerMesConnexions,
  modifierFiche,
  modifierMonProfil,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
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
import { PasswordInput } from '@/components/password-input'
import {
  ChampNouveauMotDePasse,
  LONGUEUR_MOT_DE_PASSE,
} from '@/features/auth/mot-de-passe'
import {
  ROLES,
  STATUTS_RATTACHEMENT,
} from '@/features/operateurs/commun/libelles'
import { useListe } from '@/features/operateurs/commun/referentiels'
import { AUCUN } from '@/features/operateurs/commun/saisie'
import { FormulaireFiche } from '@/features/operateurs/personnes/formulaires'
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
 * « Mon compte » (spec 02 §6.3, spec 03 §6.1), mis en page comme le Paramétrage : profil, e-mail,
 * opérateurs et opérateur actif, fiche professionnelle dans chaque opérateur, mot de passe, connexions.
 * Ouvert à tout utilisateur connecté (RG-OPE-33).
 */
const sections: SectionPage[] = [
  {
    cle: 'profil',
    titre: 'Profil',
    icon: UserRound,
    description:
      "Votre nom, tel que les autres utilisateurs le voient, et l'affichage de l'application.",
  },
  {
    cle: 'compte',
    titre: 'Mon compte',
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
      description='Vos informations, vos opérateurs et votre sécurité.'
      sections={sections}
      active={active}
      onChoisir={setActive}
    >
      {active === 'profil' && (
        <>
          <Profil />
          <Separator className='my-8' />
          <Section titre='Affichage'>
            <Affichage />
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
    </PageSections>
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

/** Titre, nom et prénom, modifiés par la personne seule ; visibles aussitôt partout (RG-OPE-47). */
function Profil() {
  const { moi } = useMoi()
  const queryClient = useQueryClient()
  const titres = useListe('titres')
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
    },
  })
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    profil.mutate()
  }
  return (
    <Section titre='Profil'>
      <Erreur erreur={profil.error} />
      <form onSubmit={soumettre} className='grid gap-4 md:grid-cols-4'>
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
        <div className='flex items-end justify-end'>
          <Button type='submit' disabled={profil.isPending}>
            Enregistrer
          </Button>
        </div>
      </form>
    </Section>
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

/** Rattachements, rôle, statut ; opérateur actif ; fiche professionnelle dans chacun (RG-OPE-33, 49). */
function MesOperateurs() {
  const { moi, actif } = useMoi()
  const choix = useChoisirOperateur()
  return (
    <>
      <Section>
        <div className='rounded-md border'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Opérateur</TableHead>
                <TableHead>Rôle</TableHead>
                <TableHead>Accès</TableHead>
                <TableHead />
              </TableRow>
            </TableHeader>
            <TableBody>
              {moi.rattachements.map((r) => (
                <TableRow key={r.id}>
                  <TableCell>
                    {r.raisonSociale}
                    <span className='text-muted-foreground'> · {r.code}</span>
                  </TableCell>
                  <TableCell>{ROLES[r.role]}</TableCell>
                  <TableCell>
                    {STATUTS_RATTACHEMENT[r.statut]}
                    {!r.operateurEnActivite && ', opérateur inactif'}
                  </TableCell>
                  <TableCell className='text-end'>
                    {r.operateur === actif?.operateur ? (
                      <Badge>Opérateur actif</Badge>
                    ) : (
                      r.utilisable && (
                        <Button
                          size='sm'
                          variant='outline'
                          disabled={choix.isPending}
                          onClick={() => choix.mutate(r.operateur)}
                        >
                          Travailler ici
                        </Button>
                      )
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </Section>
      {moi.rattachements
        .filter((r) => r.statut === 'ACTIF' && r.operateurEnActivite)
        .map((r) => (
          <Fragment key={r.id}>
            <Separator className='my-8' />
            <MaFiche rattachement={r} />
          </Fragment>
        ))}
    </>
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
  return (
    <Section titre={`Ma fiche chez ${rattachement.raisonSociale}`}>
      {m && (
        <FormulaireFiche
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
      )}
    </Section>
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

/** Ses propres connexions (RG-AUT-13). */
function MesConnexions() {
  const connexions = useQuery({
    queryKey: ['mes-connexions'],
    queryFn: () =>
      listerMesConnexions({ throwOnError: true }).then((r) => r.data),
  })
  return (
    <Section>
      <div className='rounded-md border'>
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
            {(connexions.data ?? []).map((l, i) => (
              <TableRow key={i}>
                <TableCell className='whitespace-nowrap'>
                  {dateHeure.format(new Date(l.date))}
                </TableCell>
                <TableCell>{l.libelle}</TableCell>
                <TableCell>{l.adresseIp}</TableCell>
                <TableCell className='max-w-80 truncate'>
                  {l.navigateur}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </Section>
  )
}
