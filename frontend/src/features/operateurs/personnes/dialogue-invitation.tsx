import { useState, type FormEvent } from 'react'
import { useMutation } from '@tanstack/react-query'
import { inviter, type Contact } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
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
import { cles, ROLES, ROLES_DESCRIPTION, type Role } from '../commun/libelles'

/**
 * Inviter (module Accès, gestion) un contact de l'opérateur, ou une personne nouvelle, avec un e-mail et
 * un rôle (RG-OPE-40). Le lien part par e-mail, valable 7 jours.
 */
export function DialogueInvitation({
  operateur,
  contact,
  open,
  onOpenChange,
  onInvite,
}: {
  operateur: number
  contact?: Contact
  open: boolean
  onOpenChange: (open: boolean) => void
  onInvite: () => void
}) {
  const [email, setEmail] = useState(contact?.email ?? '')
  const [role, setRole] = useState<Role>('OBSERVATEUR')
  const [nom, setNom] = useState('')
  const [prenom, setPrenom] = useState('')
  const [fonction, setFonction] = useState('')
  const [telephone, setTelephone] = useState('')

  const invitation = useMutation({
    mutationFn: () =>
      inviter({
        path: { operateur },
        body: contact
          ? { contact: contact.id, email: email.trim(), role }
          : {
              email: email.trim(),
              role,
              nom: nom.trim(),
              prenom: prenom.trim(),
              fonction: fonction.trim() || undefined,
              telephoneProfessionnel: telephone.trim() || undefined,
            },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      onOpenChange(false)
      onInvite()
    },
  })
  const soumettre = (e: FormEvent) => {
    e.preventDefault()
    invitation.mutate()
  }
  const erreur = invitation.error ? lireProbleme(invitation.error) : null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='sm:max-w-lg'>
        <DialogHeader>
          <DialogTitle>
            {contact ? `Inviter ${contact.nomComplet}` : 'Inviter une personne'}
          </DialogTitle>
          <DialogDescription>
            Un lien valable 7 jours part par e-mail. En l'acceptant, la personne
            devient membre de l'opérateur avec ce rôle
            {contact ? ', et garde sa fiche professionnelle.' : '.'}
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={soumettre} className='space-y-4'>
          {erreur && (
            <Alert variant='destructive'>
              <AlertDescription>
                {erreur.detail ?? erreur.titre}
              </AlertDescription>
            </Alert>
          )}
          {!contact && (
            <div className='grid gap-4 sm:grid-cols-2'>
              <Champ libelle='Prénom' valeur={prenom} onChange={setPrenom} />
              <Champ libelle='Nom' valeur={nom} onChange={setNom} />
              <Champ
                libelle='Fonction'
                valeur={fonction}
                onChange={setFonction}
              />
              <Champ
                libelle='Téléphone professionnel'
                valeur={telephone}
                onChange={setTelephone}
              />
            </div>
          )}
          <Champ libelle='E-mail' valeur={email} onChange={setEmail} />
          <div className='space-y-2'>
            <Label>Rôle</Label>
            <Select value={role} onValueChange={(v) => setRole(v as Role)}>
              <SelectTrigger className='w-full'>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {cles(ROLES).map((r) => (
                  <SelectItem key={r} value={r}>
                    {ROLES[r]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className='text-muted-foreground text-sm'>
              {ROLES_DESCRIPTION[role]}
            </p>
          </div>
          <div className='flex justify-end'>
            <Button type='submit' disabled={invitation.isPending}>
              Envoyer l'invitation
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}

function Champ({
  libelle,
  valeur,
  onChange,
}: {
  libelle: string
  valeur: string
  onChange: (valeur: string) => void
}) {
  return (
    <div className='space-y-2'>
      <Label>{libelle}</Label>
      <Input value={valeur} onChange={(e) => onChange(e.target.value)} />
    </div>
  )
}
