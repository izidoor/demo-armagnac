import { useState } from 'react'
import { z } from 'zod'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  consulterVendeur,
  parametrerVendeur,
  type VendeurFacturation,
} from '@/api/generated'
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { CadreEditable } from '@/components/metier/cadre-editable'
import { afficherErreur } from '@/components/metier/erreur-formulaire'
import { ErreurRacine } from '@/components/metier/erreur-racine'
import { PageMetier } from '@/components/metier/page-metier'
import { Lecture } from '@/features/operateurs/commun/champs'
import { cleFacturation } from './api'

const CLE = [...cleFacturation, 'vendeur'] as const

const obligatoire = z.string().trim().min(1, 'Obligatoire.')

const schema = z.object({
  nom: obligatoire,
  ligne1: obligatoire,
  ligne2: z.string(),
  codePostal: obligatoire,
  ville: obligatoire,
  pays: obligatoire,
  siret: obligatoire,
  numeroTva: obligatoire,
  iban: obligatoire,
  bic: obligatoire,
  telephone: obligatoire,
  email: z.string().trim().email('Un e-mail valide.'),
  tvaSurDebits: z.boolean(),
  escompte: z.string(),
  penalites: z.string(),
})

type Saisie = z.infer<typeof schema>

function saisieDe(v: VendeurFacturation | undefined): Saisie {
  return {
    nom: v?.nom ?? '',
    ligne1: v?.siege.ligne1 ?? '',
    ligne2: v?.siege.ligne2 ?? '',
    codePostal: v?.siege.codePostal ?? '',
    ville: v?.siege.ville ?? '',
    pays: v?.siege.pays ?? 'France',
    siret: v?.siret ?? '',
    numeroTva: v?.numeroTva ?? '',
    iban: v?.iban ?? '',
    bic: v?.bic ?? '',
    telephone: v?.telephone ?? '',
    email: v?.email ?? '',
    tvaSurDebits: v?.tvaSurDebits ?? false,
    escompte: v?.escompte ?? '',
    penalites: v?.penalites ?? '',
  }
}

/**
 * Paramètres de l'application > BNIA > Facturation (spec 08 §3.7, §6.1) : le vendeur, le BNIA, recopié sur chaque pièce à son émission ;
 * le modifier ne change aucune pièce émise (RG-FAC-16). Sans vendeur, l'émission est refusée (RG-FAC-09).
 * Un cadre en lecture, « Éditer » le passant en saisie.
 */
export function PageVendeur() {
  const requete = useQuery({
    queryKey: CLE,
    queryFn: () => consulterVendeur({ throwOnError: true }).then((r) => r.data),
  })
  if (!requete.data) {
    return (
      <PageMetier titre='Facturation' description='Chargement…'>
        {null}
      </PageMetier>
    )
  }
  return (
    <FormulaireVendeur
      key={JSON.stringify(requete.data.vendeur ?? null)}
      vendeur={requete.data.vendeur}
      modifiable={requete.data.modifiable}
    />
  )
}

function FormulaireVendeur({
  vendeur,
  modifiable,
}: {
  vendeur?: VendeurFacturation
  modifiable: boolean
}) {
  const queryClient = useQueryClient()
  const [edition, setEdition] = useState(false)
  const form = useForm<Saisie>({
    resolver: zodResolver(schema),
    defaultValues: saisieDe(vendeur),
  })
  const enregistrement = useMutation({
    mutationFn: (s: Saisie) =>
      parametrerVendeur({
        body: {
          nom: s.nom,
          siege: {
            ligne1: s.ligne1,
            ligne2: s.ligne2 || undefined,
            codePostal: s.codePostal,
            ville: s.ville,
            pays: s.pays,
            codePays:
              s.pays.trim().toLowerCase() === 'france' ? 'FR' : undefined,
          },
          siret: s.siret,
          numeroTva: s.numeroTva,
          iban: s.iban,
          bic: s.bic,
          telephone: s.telephone,
          email: s.email,
          tvaSurDebits: s.tvaSurDebits,
          escompte: s.escompte || undefined,
          penalites: s.penalites || undefined,
        },
        throwOnError: true,
      }),
    meta: { erreurAffichee: true },
    onSuccess: () => {
      toast.success('Vendeur enregistré.')
      setEdition(false)
      queryClient.invalidateQueries({ queryKey: cleFacturation })
    },
    onError: (error) => afficherErreur(form, error),
  })
  const champ = (
    nom: keyof Saisie,
    libelle: string,
    description?: string,
    classe = 'sm:col-span-1'
  ) => (
    <FormField
      control={form.control}
      name={nom}
      render={({ field }) => (
        <FormItem className={classe}>
          <FormLabel>{libelle}</FormLabel>
          <FormControl>
            <Input {...field} value={field.value as string} />
          </FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  )

  const v = vendeur
  const adresse = v && (
    <>
      {v.siege.ligne1}
      {v.siege.ligne2 && <br />}
      {v.siege.ligne2}
      <br />
      {v.siege.codePostal} {v.siege.ville}
      <br />
      {v.siege.pays}
    </>
  )
  const lecture = (
    <div className='space-y-4'>
      {!v && (
        <p className='text-muted-foreground text-sm'>
          Le vendeur n'est pas encore paramétré : aucune facture ne peut être
          émise.
        </p>
      )}
      <div className='grid gap-4 sm:grid-cols-2'>
        <Lecture label='Nom' valeur={v?.nom} className='sm:col-span-2' />
        <Lecture label='Adresse du siège' valeur={adresse} />
        <div className='space-y-4'>
          <Lecture label='Téléphone' valeur={v?.telephone} />
          <Lecture label='E-mail' valeur={v?.email} />
        </div>
        <Lecture label='SIRET' valeur={v?.siret} />
        <Lecture label='N° de TVA intracommunautaire' valeur={v?.numeroTva} />
        <Lecture label='IBAN' valeur={v?.iban} />
        <Lecture label='BIC' valeur={v?.bic} />
        <Lecture
          label='Escompte'
          valeur={v && (v.escompte || 'Pas d’escompte pour paiement anticipé')}
        />
        <Lecture
          label='Pénalités de retard'
          valeur={v && (v.penalites || 'Trois fois le taux d’intérêt légal')}
        />
        <Lecture
          label="Option pour le paiement de la TVA d'après les débits"
          valeur={v && (v.tvaSurDebits ? 'Oui' : 'Non')}
          className='sm:col-span-2'
        />
      </div>
    </div>
  )

  return (
    <PageMetier
      titre='Facturation'
      description='Le BNIA vendeur, tel qu’il est imprimé sur les factures ; chaque pièce garde le vendeur de son émission.'
    >
      <Form {...form}>
        <form
          className='w-full'
          onSubmit={form.handleSubmit((s) => enregistrement.mutate(s))}
        >
          <CadreEditable
            titre='Vendeur'
            edition={edition}
            modifiable={modifiable}
            onEditer={() => setEdition(true)}
            onAnnuler={() => {
              form.reset(saisieDe(vendeur))
              setEdition(false)
            }}
            enregistrement={enregistrement.isPending}
          >
            {edition ? (
              <div className='space-y-4'>
                <ErreurRacine errors={form.formState.errors} />
                <div className='grid gap-4 sm:grid-cols-2'>
                  {champ('nom', 'Nom', undefined, 'sm:col-span-2')}
                  {champ('ligne1', 'Adresse du siège')}
                  {champ('ligne2', 'Complément')}
                  {champ('codePostal', 'Code postal')}
                  {champ('ville', 'Ville')}
                  {champ('pays', 'Pays')}
                  {champ('telephone', 'Téléphone')}
                  {champ('email', 'E-mail', 'Imprimé sur les factures.')}
                  {champ('siret', 'SIRET', 'Le SIREN en est tiré.')}
                  {champ('numeroTva', 'N° de TVA intracommunautaire')}
                  {champ('iban', 'IBAN')}
                  {champ('bic', 'BIC')}
                  {champ(
                    'escompte',
                    'Escompte',
                    'Vide : « Pas d’escompte pour paiement anticipé ».'
                  )}
                  {champ(
                    'penalites',
                    'Pénalités de retard',
                    'Vide : « trois fois le taux d’intérêt légal ».'
                  )}
                  <FormField
                    control={form.control}
                    name='tvaSurDebits'
                    render={({ field }) => (
                      <FormItem className='flex items-center gap-3 sm:col-span-2'>
                        <FormControl>
                          <Switch
                            checked={field.value}
                            onCheckedChange={field.onChange}
                          />
                        </FormControl>
                        <FormLabel className='font-normal'>
                          Option pour le paiement de la TVA d'après les débits
                          (imprimée sur les factures)
                        </FormLabel>
                      </FormItem>
                    )}
                  />
                </div>
              </div>
            ) : (
              lecture
            )}
          </CadreEditable>
        </form>
      </Form>
    </PageMetier>
  )
}
