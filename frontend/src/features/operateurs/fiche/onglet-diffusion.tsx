import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { X } from 'lucide-react'
import { type FicheOperateur } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import { Switch } from '@/components/ui/switch'
import { apiOperateur } from '../commun/api'
import { Lecture } from '../commun/champs'
import {
  NIVEAUX_DIFFUSION,
  cles,
  type NiveauDiffusion,
} from '../commun/libelles'

/**
 * Onglet Diffusion : niveau d'accord et millésimes diffusables en accord partiel (RG-OPE-14) ; pour le
 * personnel du BNIA, l'accès aux statistiques du portail (RG-OPE-15), en lecture sinon (RG-OPE-36).
 */
export function OngletDiffusion({
  fiche,
  onEnregistre,
}: {
  fiche: FicheOperateur
  onEnregistre: () => void
}) {
  const gestion = fiche.droits.contacts === 'GESTION'
  const [niveau, setNiveau] = useState<NiveauDiffusion>(fiche.niveauDiffusion)
  const [millesimes, setMillesimes] = useState<number[]>(
    fiche.millesimesDiffusables
  )
  const [saisie, setSaisie] = useState('')
  const [statistiques, setStatistiques] = useState(fiche.accesStatistiques)
  const [erreur, setErreur] = useState<string | null>(null)

  const ajouter = () => {
    const annee = Number(saisie.trim())
    if (Number.isInteger(annee) && !millesimes.includes(annee)) {
      setMillesimes([...millesimes, annee].sort((a, b) => a - b))
    }
    setSaisie('')
  }

  const mutation = useMutation({
    mutationFn: () =>
      apiOperateur(fiche.id).diffusion(
        niveau,
        niveau === 'PARTIEL' ? millesimes : [],
        statistiques,
        fiche.version
      ),
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

  const vidage =
    fiche.niveauDiffusion === 'PARTIEL' &&
    niveau !== 'PARTIEL' &&
    millesimes.length > 0

  return (
    <Card>
      <CardHeader>
        <CardTitle>Diffusion des millésimes</CardTitle>
      </CardHeader>
      <CardContent className='space-y-4'>
        {erreur && (
          <Alert variant='destructive'>
            <AlertDescription>{erreur}</AlertDescription>
          </Alert>
        )}
        <RadioGroup
          value={niveau}
          onValueChange={(v) => setNiveau(v as NiveauDiffusion)}
          className='flex gap-6'
        >
          {cles(NIVEAUX_DIFFUSION).map((n) => (
            <label key={n} className='flex items-center gap-2 text-sm'>
              <RadioGroupItem value={n} /> {NIVEAUX_DIFFUSION[n]}
            </label>
          ))}
        </RadioGroup>
        {vidage && (
          <Alert>
            <AlertDescription>
              En quittant l'accord partiel, la liste des millésimes diffusables
              sera vidée à l'enregistrement.
            </AlertDescription>
          </Alert>
        )}
        {niveau === 'PARTIEL' && (
          <div className='space-y-2'>
            <Label>Millésimes diffusables</Label>
            <div className='flex flex-wrap gap-2'>
              {millesimes.map((m) => (
                <Badge key={m} variant='secondary' className='gap-1'>
                  {m}
                  <button
                    type='button'
                    onClick={() =>
                      setMillesimes(millesimes.filter((x) => x !== m))
                    }
                  >
                    <X className='size-3' />
                    <span className='sr-only'>Retirer {m}</span>
                  </button>
                </Badge>
              ))}
            </div>
            <div className='flex gap-2'>
              <Input
                value={saisie}
                onChange={(e) => setSaisie(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    e.preventDefault()
                    ajouter()
                  }
                }}
                placeholder='2005'
                className='w-32'
              />
              <Button type='button' variant='outline' onClick={ajouter}>
                Ajouter
              </Button>
            </div>
          </div>
        )}
        {fiche.droits.personnelBnia && gestion ? (
          <label className='flex items-center gap-2 text-sm'>
            <Switch checked={statistiques} onCheckedChange={setStatistiques} />
            Accès aux statistiques du portail
          </label>
        ) : (
          <Lecture
            label='Accès aux statistiques du portail'
            valeur={fiche.accesStatistiques ? 'Oui' : 'Non'}
          />
        )}
        {gestion && (
          <div className='flex justify-end'>
            <Button
              onClick={() => mutation.mutate()}
              disabled={mutation.isPending}
            >
              Enregistrer
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
