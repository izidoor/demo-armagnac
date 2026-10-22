import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { type FicheOperateur } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
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
import { CadreEditable } from '@/components/metier/cadre-editable'
import { apiOperateur, useAnnuaire } from '../commun/api'
import { Lecture } from '../commun/champs'
import { AUCUN } from '../commun/saisie'

type Mode = 'personne' | 'adresse' | 'aucun'

/**
 * Contact principal (RG-OPE-22) et destinataire des factures (RG-OPE-23) : une personne de l'annuaire
 * (contact ou membre, par sa fiche professionnelle), avec e-mail pour les factures, ou une adresse saisie
 * (bascule « une personne de l'annuaire » / « une adresse »). Le parent le remonte (clé) quand la fiche
 * change. Affiché en lecture ; « Éditer » le passe en saisie, sous le contrôle du parent (un cadre en
 * édition à la fois). Sans la gestion du module Contacts, pas d'« Éditer ».
 */
export function Designations({
  fiche,
  edition,
  bloque,
  onEditer,
  onFermer,
  onEnregistre,
}: {
  fiche: FicheOperateur
  edition: boolean
  bloque: boolean
  onEditer: () => void
  onFermer: () => void
  onEnregistre: () => void
}) {
  const modifiable = fiche.droits.contacts === 'GESTION'
  const principalInitial = fiche.contactPrincipal
    ? String(fiche.contactPrincipal)
    : AUCUN
  const modeInitial: Mode = fiche.destinataireFacturesFiche
    ? 'personne'
    : fiche.destinataireFacturesEmail
      ? 'adresse'
      : 'aucun'
  const destinataireInitial = fiche.destinataireFacturesFiche
    ? String(fiche.destinataireFacturesFiche)
    : AUCUN
  const adresseInitiale = fiche.destinataireFacturesEmail ?? ''
  const [principal, setPrincipal] = useState(principalInitial)
  const [mode, setMode] = useState<Mode>(modeInitial)
  const [destinataire, setDestinataire] = useState(destinataireInitial)
  const [adresse, setAdresse] = useState(adresseInitiale)
  const [erreur, setErreur] = useState<string | null>(null)
  const annuler = () => {
    setPrincipal(principalInitial)
    setMode(modeInitial)
    setDestinataire(destinataireInitial)
    setAdresse(adresseInitiale)
    setErreur(null)
    onFermer()
  }

  const annuaire = useAnnuaire(fiche.id)
  const personnes = (annuaire.data ?? []).filter((p) => p.fiche != null)
  const avecEmail = personnes.filter((p) => p.email)
  const nomDe = (id?: number | null) =>
    id == null
      ? undefined
      : (personnes.find((p) => p.fiche === id)?.nomComplet ?? `n° ${id}`)

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
      onFermer()
      onEnregistre()
    },
    onError: (error) => {
      const p = lireProbleme(error)
      setErreur(p.regle ? `${p.detail} (${p.regle})` : (p.detail ?? p.titre))
    },
  })

  const factures = fiche.emailFacturation && (
    <p className='text-muted-foreground text-sm'>
      Les factures partent à {fiche.emailFacturation}.
    </p>
  )

  return (
    <CadreEditable
      titre='Contact principal et destinataire des factures'
      edition={edition}
      modifiable={modifiable}
      bloque={bloque}
      onEditer={onEditer}
      onAnnuler={annuler}
      onEnregistrer={() => mutation.mutate()}
      enregistrement={mutation.isPending}
    >
      {edition ? (
        <div className='grid gap-4 @min-[48rem]/content:grid-cols-2'>
          {erreur && (
            <Alert
              variant='destructive'
              className='@min-[48rem]/content:col-span-2'
            >
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
              <Select value={destinataire} onValueChange={setDestinataire}>
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
                placeholder='compta@exemple.fr'
              />
            )}
            {factures}
          </div>
        </div>
      ) : (
        <div className='grid gap-4 @min-[48rem]/content:grid-cols-2'>
          <Lecture
            label='Contact principal'
            valeur={nomDe(fiche.contactPrincipal)}
          />
          <div className='space-y-1'>
            <Lecture
              label='Destinataire des factures'
              valeur={
                fiche.destinataireFacturesFiche
                  ? nomDe(fiche.destinataireFacturesFiche)
                  : fiche.destinataireFacturesEmail
              }
            />
            {factures}
          </div>
        </div>
      )}
    </CadreEditable>
  )
}
