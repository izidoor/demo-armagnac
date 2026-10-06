import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { type FicheOperateur } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { apiOperateur, type Espace } from '../commun/api'
import { AUCUN } from '../commun/saisie'

type Mode = 'contact' | 'adresse' | 'aucun'

/**
 * Contact principal (RG-OPE-22) et destinataire des factures (RG-OPE-23) : un contact actif, avec
 * e-mail pour les factures, ou une adresse saisie (bascule « un contact » / « une adresse »). Le parent
 * le remonte (clé) quand la fiche change.
 */
export function Designations({
  espace,
  fiche,
  onEnregistre,
}: {
  espace: Espace
  fiche: FicheOperateur
  onEnregistre: () => void
}) {
  const [principal, setPrincipal] = useState(
    fiche.contactPrincipal ? String(fiche.contactPrincipal) : AUCUN
  )
  const [mode, setMode] = useState<Mode>(
    fiche.destinataireFacturesContact
      ? 'contact'
      : fiche.destinataireFacturesEmail
        ? 'adresse'
        : 'aucun'
  )
  const [contact, setContact] = useState(
    fiche.destinataireFacturesContact
      ? String(fiche.destinataireFacturesContact)
      : AUCUN
  )
  const [adresse, setAdresse] = useState(fiche.destinataireFacturesEmail ?? '')
  const [erreur, setErreur] = useState<string | null>(null)

  const actifs = fiche.contacts.filter((c) => c.actif)
  const avecEmail = actifs.filter((c) => c.email)

  const mutation = useMutation({
    mutationFn: () =>
      apiOperateur(espace, fiche.id).designer({
        contactPrincipal: principal === AUCUN ? undefined : Number(principal),
        destinataireFacturesContact:
          mode === 'contact' && contact !== AUCUN ? Number(contact) : undefined,
        destinataireFacturesEmail:
          mode === 'adresse' && adresse.trim() ? adresse.trim() : undefined,
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      setErreur(null)
      onEnregistre()
    },
    onError: (error) => {
      const p = lireProbleme(error)
      setErreur(p.regle ? `${p.detail} (${p.regle})` : (p.detail ?? p.titre))
    },
  })

  return (
    <Card>
      <CardHeader>
        <CardTitle>Contact principal et destinataire des factures</CardTitle>
      </CardHeader>
      <CardContent className='grid gap-4 md:grid-cols-2'>
        {erreur && (
          <Alert variant='destructive' className='md:col-span-2'>
            <AlertDescription>{erreur}</AlertDescription>
          </Alert>
        )}
        <div className='space-y-2'>
          <Label>Contact principal</Label>
          <Select value={principal} onValueChange={setPrincipal}>
            <SelectTrigger className='w-full'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={AUCUN}>—</SelectItem>
              {actifs.map((c) => (
                <SelectItem key={c.id} value={String(c.id)}>
                  {c.nomComplet}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className='space-y-2'>
          <Label>Destinataire des factures</Label>
          <RadioGroup
            value={mode}
            onValueChange={(v) => setMode(v as Mode)}
            className='flex gap-4'
          >
            {(
              [
                ['contact', 'Un contact'],
                ['adresse', 'Une adresse'],
                ['aucun', 'Aucun'],
              ] as const
            ).map(([valeur, libelle]) => (
              <label key={valeur} className='flex items-center gap-2 text-sm'>
                <RadioGroupItem value={valeur} /> {libelle}
              </label>
            ))}
          </RadioGroup>
          {mode === 'contact' && (
            <Select value={contact} onValueChange={setContact}>
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='Contact actif avec e-mail' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={AUCUN}>—</SelectItem>
                {avecEmail.map((c) => (
                  <SelectItem key={c.id} value={String(c.id)}>
                    {c.nomComplet} ({c.email})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
          {mode === 'adresse' && (
            <Input
              value={adresse}
              onChange={(e) => setAdresse(e.target.value)}
              placeholder='compta@exemple.fr'
            />
          )}
          {fiche.emailFacturation && (
            <p className='text-muted-foreground text-sm'>
              Les factures partent à {fiche.emailFacturation}.
            </p>
          )}
        </div>
        <div className='flex justify-end md:col-span-2'>
          <Button
            onClick={() => mutation.mutate()}
            disabled={mutation.isPending}
          >
            Enregistrer les désignations
          </Button>
        </div>
      </CardContent>
    </Card>
  )
}
