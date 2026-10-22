import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { ChevronDown, Mail, Phone, Plus } from 'lucide-react'
import {
  creerContact,
  type Contact,
  type Droits,
  type PersonneAnnuaire,
} from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardAction,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from '@/components/ui/collapsible'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { CelluleOuvrir, EnteteOuvrir } from '@/components/metier/cellule-ouvrir'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { cleOperateur, useAnnuaire, type Espace } from '../commun/api'
import { ROLES } from '../commun/libelles'
import { DialogueInvitation } from './dialogue-invitation'
import { FormulaireContact } from './formulaires'
import { OngletJournal } from './journal'
import { genreDe, useNavigationPersonnes } from './navigation'

/**
 * Annuaire d'un opérateur (RG-OPE-52) : contacts, membres et personnes nouvelles invitées, en une liste ;
 * déclarer un contact (module Contacts) ; inviter (module Accès) ; au back-office, le journal en dessous. Sous 56rem de
 * contenu (730 px de tableau, plus 84 px de marges et de carte), une carte par personne à la place du tableau.
 */
export function OngletAnnuaire({
  espace,
  operateur,
  droits,
  avecJournal = false,
}: {
  espace: Espace
  operateur: number
  droits: Droits
  /** Le journal (RG-OPE-34) dans un cadre dépliable sous la liste ; au portail, il a son propre onglet. */
  avecJournal?: boolean
}) {
  const queryClient = useQueryClient()
  const annuaire = useAnnuaire(operateur)
  const navigation = useNavigationPersonnes(espace, operateur)
  const [creation, setCreation] = useState(false)
  const [invitation, setInvitation] = useState(false)
  const rafraichir = () =>
    queryClient.invalidateQueries({ queryKey: cleOperateur(operateur) })
  const personnes = annuaire.data ?? []

  return (
    <div className='space-y-4'>
      <Card>
        <CardHeader>
          <CardTitle>Personnes</CardTitle>
          <CardAction className='flex flex-wrap justify-end gap-2'>
            {droits.contacts === 'GESTION' && (
              <Button variant='outline' onClick={() => setCreation(true)}>
                <Plus /> Déclarer un contact
              </Button>
            )}
            {droits.acces === 'GESTION' && (
              <Button onClick={() => setInvitation(true)}>
                <Mail /> Inviter une personne
              </Button>
            )}
          </CardAction>
        </CardHeader>
        <CardContent>
          <div className='rounded-md border @max-[56rem]/content:hidden'>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nom</TableHead>
                  <TableHead>Fonction</TableHead>
                  <TableHead>E-mail</TableHead>
                  <TableHead>Téléphone</TableHead>
                  <TableHead>État</TableHead>
                  <EnteteOuvrir />
                </TableRow>
              </TableHeader>
              <TableBody>
                {personnes.length === 0 && (
                  <TableRow>
                    <TableCell
                      colSpan={6}
                      className='text-muted-foreground h-16 text-center'
                    >
                      {annuaire.isPending ? 'Chargement…' : 'Personne.'}
                    </TableCell>
                  </TableRow>
                )}
                {personnes.map((p) => (
                  <TableRow
                    key={`${p.genre}-${p.id}`}
                    className='cursor-pointer'
                    onClick={() => navigation.ouvrir(genreDe(p), p.id)}
                  >
                    <TableCell>{p.nomComplet}</TableCell>
                    <TableCell>{p.fonction}</TableCell>
                    <TableCell>{p.email}</TableCell>
                    <TableCell>{p.telephoneProfessionnel}</TableCell>
                    <TableCell>
                      <EtatPersonne personne={p} />
                    </TableCell>
                    <CelluleOuvrir
                      libelle={`Ouvrir la fiche de ${p.nomComplet}`}
                      onOuvrir={() => navigation.ouvrir(genreDe(p), p.id)}
                    />
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
          <CartesListe
            requete={annuaire}
            vide='Personne.'
            masquee='@min-[56rem]/content:hidden'
          >
            {personnes.map((p) => (
              <CarteListe
                key={`${p.genre}-${p.id}`}
                onOuvrir={() => navigation.ouvrir(genreDe(p), p.id)}
                titre={p.nomComplet}
              >
                {p.fonction && (
                  <div className='text-muted-foreground text-sm'>
                    {p.fonction}
                  </div>
                )}
                <EtatPersonne personne={p} />
                {(p.email || p.telephoneProfessionnel) && (
                  <div className='flex flex-wrap gap-x-4 gap-y-1 text-sm'>
                    {p.telephoneProfessionnel && (
                      <a
                        href={`tel:${p.telephoneProfessionnel.replace(/\s/g, '')}`}
                        className='relative z-10 inline-flex items-center gap-1.5 hover:underline'
                      >
                        <Phone className='text-muted-foreground size-3.5' />
                        {p.telephoneProfessionnel}
                      </a>
                    )}
                    {p.email && (
                      <a
                        href={`mailto:${p.email}`}
                        className='relative z-10 inline-flex min-w-0 items-center gap-1.5 hover:underline'
                      >
                        <Mail className='text-muted-foreground size-3.5 shrink-0' />
                        <span className='truncate'>{p.email}</span>
                      </a>
                    )}
                  </div>
                )}
              </CarteListe>
            ))}
          </CartesListe>
        </CardContent>
      </Card>
      {avecJournal && (
        <Collapsible asChild className='group'>
          <Card>
            <CardHeader>
              <CollapsibleTrigger className='hover:text-primary flex w-full items-center justify-between text-left'>
                <CardTitle>Journal des accès utilisateurs</CardTitle>
                <ChevronDown className='text-muted-foreground size-4 shrink-0 transition-transform group-data-[state=open]:rotate-180' />
              </CollapsibleTrigger>
            </CardHeader>
            <CollapsibleContent>
              <CardContent>
                <OngletJournal operateur={operateur} />
              </CardContent>
            </CollapsibleContent>
          </Card>
        </Collapsible>
      )}
      <DialogueFormulaire
        ouvert={creation}
        onOuvertChange={setCreation}
        titre='Déclarer un contact'
        description='Une personne sans compte, avec sa fiche professionnelle. Elle pourra ensuite être invitée.'
        className='sm:max-w-3xl'
      >
        <FormulaireContact
          enregistrer={(body) =>
            creerContact({
              path: { operateur },
              body,
              throwOnError: true,
            }).then((r) => r.data)
          }
          onSucces={(cree) => {
            setCreation(false)
            rafraichir()
            navigation.ouvrir('contact', (cree as Contact).id)
          }}
        />
      </DialogueFormulaire>
      {invitation && (
        <DialogueInvitation
          operateur={operateur}
          personnes={personnes}
          open={invitation}
          onOpenChange={setInvitation}
          onInvite={rafraichir}
        />
      )}
    </div>
  )
}

/** Ce qu'est la personne pour l'opérateur (contact, rôle) et ce qui la concerne (suspension, blocage, invitation). */
function EtatPersonne({ personne: p }: { personne: PersonneAnnuaire }) {
  return (
    <span className='flex flex-wrap gap-1'>
      {p.genre === 'CONTACT' && <Badge variant='outline'>Contact</Badge>}
      {p.genre === 'MEMBRE' && p.role && <Badge>{ROLES[p.role]}</Badge>}
      {p.statut === 'SUSPENDU' && (
        <Badge variant='secondary'>Accès suspendu</Badge>
      )}
      {p.bloqueParBnia && (
        <Badge variant='destructive'>Bloqué par le BNIA</Badge>
      )}
      {(p.genre === 'INVITATION' || p.invitationEnAttente) && (
        <Badge variant='secondary'>
          Invitation envoyée
          {p.genre === 'INVITATION' && p.role ? ` (${ROLES[p.role]})` : ''}
        </Badge>
      )}
    </span>
  )
}
