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
import { apiOperateur, useAnnuaire } from '../commun/api'
import { AUCUN } from '../commun/saisie'

type Mode = 'personne' | 'adresse' | 'aucun'

/**
 * Contact principal (RG-OPE-22) et destinataire des factures (RG-OPE-23) : une personne de l'annuaire
 * (contact ou membre, par sa fiche professionnelle), avec e-mail pour les factures, ou une adresse saisie
 * (bascule « une personne de l'annuaire » / « une adresse »). Le parent le remonte (clé) quand la fiche
 * change. En lecture seule sans la gestion du module Contacts.
 */
export function Designations({
  fiche,
  onEnregistre,
}: {
  fiche: FicheOperateur
  onEnregistre: () => void
}) {
  const modifiable = fiche.droits.contacts === 'GESTION'
  const [principal, setPrincipal] = useState(
    fiche.contactPrincipal ? String(fiche.contactPrincipal) : AUCUN
  )
  const [mode, setMode] = useState<Mode>(
    fiche.destinataireFacturesFiche
      ? 'personne'
      : fiche.destinataireFacturesEmail
        ? 'adresse'
        : 'aucun'
  )
  const [destinataire, setDestinataire] = useState(
    fiche.destinataireFacturesFiche
      ? String(fiche.destinataireFacturesFiche)
      : AUCUN
  )
  const [adresse, setAdresse] = useState(fiche.destinataireFacturesEmail ?? '')
  const [erreur, setErreur] = useState<string | null>(null)

  const annuaire = useAnnuaire(fiche.id)
  const personnes = (annuaire.data ?? []).filter((p) => p.fiche != null)
  const avecEmail = personnes.filter((p) => p.email)

  const mutation = useMutation({
    mutationFn: () =>
      apiOperateur(fiche.id).designer({
        version: fiche.version,
        contactPrincipal: principal === AUCUN ? undefined : Number(principal),
        destinataireFacturesFiche:
          mode === 'personne' && destinataire !== AUCUN
            ? Number(destinataire)
            : undefined,
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
          <Select
            value={principal}
            onValueChange={setPrincipal}
            disabled={!modifiable}
          >
            <SelectTrigger className='w-full'>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={AUCUN}>—</SelectItem>
              {personnes.map((p) => (
                <SelectItem key={p.fiche} value={String(p.fiche)}>
                  {p.nomComplet}
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
            className='flex flex-wrap gap-4'
            disabled={!modifiable}
          >
            {(
              [
                ['personne', "Une personne de l'annuaire"],
                ['adresse', 'Une adresse'],
                ['aucun', 'Aucun'],
              ] as const
            ).map(([valeur, libelle]) => (
              <label key={valeur} className='flex items-center gap-2 text-sm'>
                <RadioGroupItem value={valeur} /> {libelle}
              </label>
            ))}
          </RadioGroup>
          {mode === 'personne' && (
            <Select
              value={destinataire}
              onValueChange={setDestinataire}
              disabled={!modifiable}
            >
              <SelectTrigger className='w-full'>
                <SelectValue placeholder='Personne avec e-mail' />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={AUCUN}>—</SelectItem>
                {avecEmail.map((p) => (
                  <SelectItem key={p.fiche} value={String(p.fiche)}>
                    {p.nomComplet} ({p.email})
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
          {mode === 'adresse' && (
            <Input
              value={adresse}
              onChange={(e) => setAdresse(e.target.value)}
              disabled={!modifiable}
              placeholder='compta@exemple.fr'
            />
          )}
          {fiche.emailFacturation && (
            <p className='text-muted-foreground text-sm'>
              Les factures partent à {fiche.emailFacturation}.
            </p>
          )}
        </div>
        {modifiable && (
          <div className='flex justify-end md:col-span-2'>
            <Button
              onClick={() => mutation.mutate()}
              disabled={mutation.isPending}
            >
              Enregistrer les désignations
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
