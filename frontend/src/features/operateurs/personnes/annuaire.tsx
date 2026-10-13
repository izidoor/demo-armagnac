import { useState } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { Mail, Plus } from 'lucide-react'
import { creerContact, type Contact, type Droits } from '@/api/generated'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { cleOperateur, useAnnuaire, type Espace } from '../commun/api'
import { ROLES } from '../commun/libelles'
import { DialogueInvitation } from './dialogue-invitation'
import { FormulaireContact } from './formulaires'
import { genreDe, useNavigationPersonnes } from './navigation'

/**
 * Annuaire d'un opérateur (RG-OPE-52) : contacts, membres et personnes nouvelles invitées, en une liste ;
 * déclarer un contact (module Contacts) ; inviter (module Accès).
 */
export function OngletAnnuaire({
  espace,
  operateur,
  droits,
}: {
  espace: Espace
  operateur: number
  droits: Droits
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
      <div className='flex flex-wrap justify-end gap-2'>
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
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nom</TableHead>
              <TableHead>Fonction</TableHead>
              <TableHead>E-mail</TableHead>
              <TableHead>Téléphone</TableHead>
              <TableHead>État</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {personnes.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={5}
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
                  <span className='flex flex-wrap gap-1'>
                    {p.genre === 'CONTACT' && (
                      <Badge variant='outline'>Contact</Badge>
                    )}
                    {p.genre === 'MEMBRE' && p.role && (
                      <Badge>{ROLES[p.role]}</Badge>
                    )}
                    {p.statut === 'SUSPENDU' && (
                      <Badge variant='secondary'>Accès suspendu</Badge>
                    )}
                    {p.bloqueParBnia && (
                      <Badge variant='destructive'>Bloqué par le BNIA</Badge>
                    )}
                    {(p.genre === 'INVITATION' || p.invitationEnAttente) && (
                      <Badge variant='secondary'>
                        Invitation envoyée
                        {p.genre === 'INVITATION' && p.role
                          ? ` (${ROLES[p.role]})`
                          : ''}
                      </Badge>
                    )}
                  </span>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <Dialog open={creation} onOpenChange={setCreation}>
        <DialogContent className='sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>Déclarer un contact</DialogTitle>
            <DialogDescription>
              Une personne sans compte, avec sa fiche professionnelle. Elle
              pourra ensuite être invitée.
            </DialogDescription>
          </DialogHeader>
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
        </DialogContent>
      </Dialog>
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
