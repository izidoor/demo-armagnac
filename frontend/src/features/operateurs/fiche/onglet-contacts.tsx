import { useState } from 'react'
import { useNavigate } from '@tanstack/react-router'
import { Plus } from 'lucide-react'
import { type Contact, type FicheOperateur } from '@/api/generated'
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
import { BadgeEtat } from '@/components/metier/badge-etat'
import { apiOperateur, type Espace } from '../commun/api'
import { FormulaireContact } from '../contacts/formulaire-contact'

/** Onglet Contacts : liste, création, ouverture (spec 02, §6.1 et §6.2). */
export function OngletContacts({
  espace,
  fiche,
  onEnregistre,
}: {
  espace: Espace
  fiche: FicheOperateur
  onEnregistre: () => void
}) {
  const navigate = useNavigate()
  const [creation, setCreation] = useState(false)
  const ouvrir = (contact: number) =>
    espace === 'bo'
      ? navigate({
          to: '/bo/operateurs/$id/contacts/$contact',
          params: { id: String(fiche.id), contact: String(contact) },
        })
      : navigate({
          to: '/portail/contacts/$contact',
          params: { contact: String(contact) },
        })

  return (
    <div className='space-y-4'>
      <div className='flex justify-end'>
        <Button onClick={() => setCreation(true)}>
          <Plus /> Nouveau contact
        </Button>
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Nom</TableHead>
              <TableHead>Fonction</TableHead>
              <TableHead>E-mail</TableHead>
              <TableHead>Téléphone</TableHead>
              <TableHead>Portail</TableHead>
              <TableHead>État</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {fiche.contacts.length === 0 && (
              <TableRow>
                <TableCell
                  colSpan={6}
                  className='text-muted-foreground h-16 text-center'
                >
                  Aucun contact.
                </TableCell>
              </TableRow>
            )}
            {fiche.contacts.map((c) => (
              <TableRow
                key={c.id}
                className='cursor-pointer'
                onClick={() => ouvrir(c.id)}
              >
                <TableCell>
                  {c.nomComplet}
                  {fiche.contactPrincipal === c.id && (
                    <Badge variant='outline' className='ms-2'>
                      Principal
                    </Badge>
                  )}
                </TableCell>
                <TableCell>{c.fonction}</TableCell>
                <TableCell>{c.email}</TableCell>
                <TableCell>{c.telephone}</TableCell>
                <TableCell>
                  {c.administrateur ? (
                    <Badge>Administrateur</Badge>
                  ) : c.acces ? (
                    <Badge variant='secondary'>Accès</Badge>
                  ) : null}
                </TableCell>
                <TableCell>
                  <BadgeEtat actif={c.actif} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <Dialog open={creation} onOpenChange={setCreation}>
        <DialogContent className='sm:max-w-3xl'>
          <DialogHeader>
            <DialogTitle>Nouveau contact</DialogTitle>
            <DialogDescription>
              Actif, sans accès au portail : l'accès s'accorde ensuite, sur sa
              fiche.
            </DialogDescription>
          </DialogHeader>
          <FormulaireContact
            libelle='Créer'
            enregistrer={(saisie) =>
              apiOperateur(espace, fiche.id).creerContact(saisie)
            }
            onSucces={(cree) => {
              setCreation(false)
              onEnregistre()
              ouvrir((cree as Contact).id)
            }}
          />
        </DialogContent>
      </Dialog>
    </div>
  )
}
