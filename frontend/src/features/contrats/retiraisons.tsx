import { useEffect, useRef, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import {
  AlertTriangle,
  Check,
  ChevronRight,
  CircleCheck,
  Pencil,
  Plus,
  Truck,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import {
  type Contrat,
  type RetiraisonContrat,
  type SaisieRetiraison,
} from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { cn } from '@/lib/utils'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Checkbox } from '@/components/ui/checkbox'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { ConfirmDialog } from '@/components/confirm-dialog'
import { BadgePlein } from '@/components/metier/badge-statut'
import { DialogueFormulaire } from '@/components/metier/dialogue-formulaire'
import { ActionDanger, ZoneDanger } from '@/components/metier/zone-danger'
import { formatDate, formatDateHeure } from '@/features/declarations/format'
import { aDesRetiraisons, droitsRetiraisons } from './acces-retiraisons'
import { apiContrats, cleContrats, type Espace } from './api'
import {
  designation,
  formatDegre,
  formatHl,
  formatHlAp,
  lireNombre,
  volumeAp,
} from './format'
import { ETATS_RETIRAISON, PARTIES } from './libelles'
import { Surligne } from './surligne'

/** La retiraison ouverte en saisie : rien, une nouvelle ({}), ou celle qu'on modifie. */
export type SaisieOuverte = { retiraison?: RetiraisonContrat } | undefined

/**
 * Les retiraisons d'un contrat conclu (spec 06 §6.2, RG-CA-13 à 16), dans la page du contrat, sur ordinateur : le
 * reste à retirer par ligne, puis chaque retiraison, ce qu'elle attend et, après une modification, son écart surligné.
 * L'acheteur déclare et retire ; la partie attendue valide ou modifie. Au vendeur, une retiraison que son stock estimé
 * ne couvre pas est signalée et ne se valide pas ; l'une ou l'autre partie clôture les retiraisons. Sur mobile, elles
 * ont leur page (`PageRetiraisons`) ; un lien vers elles qui arrive sur ordinateur ouvre le contrat à cette carte
 * (`#retiraisons`).
 */
export function CarteRetiraisons({
  espace,
  operateur,
  contrat: c,
  className,
}: {
  espace: Espace
  operateur: number
  contrat: Contrat
  className?: string
}) {
  const [saisie, setSaisie] = useState<SaisieOuverte>()
  const carte = useRef<HTMLDivElement>(null)
  const { agit, peutDeclarer } = droitsRetiraisons(espace, c)
  useEffect(() => {
    if (window.location.hash === '#retiraisons')
      carte.current?.scrollIntoView({ block: 'start' })
  }, [])
  if (!aDesRetiraisons(c)) return null
  return (
    <Card
      ref={carte}
      id='retiraisons'
      className={cn('scroll-mt-20', className)}
    >
      <CardHeader className='flex flex-row flex-wrap items-center justify-between gap-2'>
        <CardTitle>Retiraisons</CardTitle>
        {peutDeclarer && (
          <Button onClick={() => setSaisie({})}>
            <Plus /> Déclarer une retiraison
          </Button>
        )}
      </CardHeader>
      <CardContent className='space-y-4'>
        <RestesARetirer contrat={c} />
        <ListeRetiraisons
          operateur={operateur}
          contrat={c}
          agit={agit}
          onModifier={(r) => setSaisie({ retiraison: r })}
        />
      </CardContent>
      {saisie && (
        <FormulaireRetiraison
          operateur={operateur}
          contrat={c}
          retiraison={saisie.retiraison}
          onFermer={() => setSaisie(undefined)}
        />
      )}
    </Card>
  )
}

/**
 * Sur mobile, dans la page du contrat, sous ce qu'il attend : l'accès à la page des retiraisons, avec le nombre de
 * celles qui attendent ma partie.
 */
export function AccesRetiraisons({
  espace,
  contrat: c,
}: {
  espace: Espace
  contrat: Contrat
}) {
  const { agit, aValider } = droitsRetiraisons(espace, c)
  if (!aDesRetiraisons(c)) return null
  const nombre = c.retiraisons.length
  return (
    <Button
      asChild
      size='lg'
      variant={agit ? 'default' : 'outline'}
      className='h-auto w-full justify-between py-3 md:hidden'
    >
      <Link
        to={
          espace === 'bo'
            ? '/bo/contrats/$id/retiraisons'
            : '/portail/contrats/$id/retiraisons'
        }
        params={{ id: String(c.id) }}
      >
        <span className='flex items-center gap-2'>
          <Truck />
          {agit ? 'Gérer les retiraisons' : 'Voir les retiraisons'}
        </span>
        <span className='flex items-center gap-2 text-sm font-normal'>
          {aValider > 0 ? (
            <BadgePlein statut='warning'>{aValider} à valider</BadgePlein>
          ) : (
            nombre > 0 && <span className='opacity-80'>{nombre}</span>
          )}
          <ChevronRight />
        </span>
      </Link>
    </Button>
  )
}

/**
 * « Clôturer les retiraisons » (RG-CA-17), pour l'une ou l'autre partie d'un contrat conclu, dans le cadre « Fin des
 * retiraisons », en bas de la page : il n'aura plus de retiraison et passe à « exécuté », même s'il reste du volume ;
 * les retiraisons non validées tombent, les validées restent.
 */
export function CloturerRetiraisons({
  operateur,
  contrat: c,
  className,
}: {
  operateur: number
  contrat: Contrat
  className?: string
}) {
  const queryClient = useQueryClient()
  const [confirmation, setConfirmation] = useState(false)
  const api = apiContrats(operateur)
  const enCours = c.retiraisons.some((r) => r.etat !== 'VALIDEE')
  const cloturer = useMutation({
    mutationFn: () => api.cloturer(c.id),
    onSuccess: () => {
      setConfirmation(false)
      toast.success('Retiraisons clôturées : le contrat est exécuté.')
      queryClient.invalidateQueries({ queryKey: cleContrats })
    },
    onError: (e) => {
      const probleme = lireProbleme(e)
      toast.error(probleme.detail ?? probleme.titre)
      queryClient.invalidateQueries({ queryKey: cleContrats })
    },
    meta: { erreurAffichee: true },
  })
  return (
    <ZoneDanger titre='Fin des retiraisons' className={className}>
      <ActionDanger
        titre='Clôturer les retiraisons'
        description={`Le contrat n'aura plus de retiraison : il passe à «\u00a0exécuté\u00a0», même s'il reste du volume à retirer ; l'autre partie en est avertie par e-mail.${enCours ? ' Ses retiraisons non validées sont supprimées ; les validées restent.' : ''}`}
      >
        <Button variant='outline' onClick={() => setConfirmation(true)}>
          <CircleCheck /> Clôturer les retiraisons
        </Button>
      </ActionDanger>
      <ConfirmDialog
        open={confirmation}
        onOpenChange={setConfirmation}
        feuilleMobile
        title={
          <>
            Clôturer les retiraisons du contrat{' '}
            <span className='whitespace-nowrap'>{c.numero}</span> ?
          </>
        }
        desc={
          "Le contrat n'aura plus de retiraison : il passe à « exécuté », même s'il reste du volume à retirer ; l'autre partie en est avertie par e-mail." +
          (enCours
            ? ' Les retiraisons non validées seront supprimées ; les validées restent.'
            : '')
        }
        confirmText='Clôturer'
        isLoading={cloturer.isPending}
        handleConfirm={() => cloturer.mutate()}
      />
    </ZoneDanger>
  )
}

/** Le reste à retirer de chaque ligne : retiré, à valider, reste, sur le volume du contrat. */
export function RestesARetirer({ contrat: c }: { contrat: Contrat }) {
  return (
    <ul className='space-y-1 text-sm'>
      {c.version.lignes.map((l, rang) => {
        const r = c.restes[rang]
        return (
          <li
            key={rang}
            className='flex flex-wrap justify-between gap-x-3 gap-y-0.5'
          >
            <span className='font-medium'>{designation(l)}</span>
            <span className='text-muted-foreground tabular-nums'>
              {formatHl(r.retire)} retirés
              {r.enCours > 0 && ` · ${formatHl(r.enCours)} à valider`} ·{' '}
              <span className='text-foreground font-medium'>
                reste {formatHl(r.reste)}
              </span>{' '}
              sur {formatHl(l.volume)}
            </span>
          </li>
        )
      })}
    </ul>
  )
}

/** Chaque retiraison, de la première à la dernière ; sans retiraison, ce qui l'amènera. */
export function ListeRetiraisons({
  operateur,
  contrat: c,
  agit,
  onModifier,
}: {
  operateur: number
  contrat: Contrat
  agit: boolean
  onModifier: (r: RetiraisonContrat) => void
}) {
  if (c.retiraisons.length === 0)
    return (
      <p className='text-muted-foreground text-sm'>
        {c.etat === 'CONCLU'
          ? c.maPartie === 'VENDEUR'
            ? "Aucune retiraison : l'acheteur la déclare, vous la validez."
            : 'Aucune retiraison pour le moment.'
          : 'Aucune retiraison.'}
      </p>
    )
  return c.retiraisons.map((r) => (
    <BlocRetiraison
      key={r.numero}
      operateur={operateur}
      contrat={c}
      retiraison={r}
      agit={agit}
      onModifier={() => onModifier(r)}
    />
  ))
}

/** Une retiraison : état, date, lignes, auteur ; l'écart surligné ; les actions de la partie attendue. */
function BlocRetiraison({
  operateur,
  contrat: c,
  retiraison: r,
  agit,
  onModifier,
}: {
  operateur: number
  contrat: Contrat
  retiraison: RetiraisonContrat
  agit: boolean
  onModifier: () => void
}) {
  const queryClient = useQueryClient()
  const [retrait, setRetrait] = useState(false)
  const api = apiContrats(operateur)
  const champ = (nom: string) => r.ecart?.champs.find((x) => x.champ === nom)
  const changementLigne = (rang: number) =>
    r.ecart?.lignes
      .find((x) => x.rang === rang && x.nature !== 'RETIREE')
      ?.champs.find((x) => x.champ === 'volume')
  const aMoi = agit && r.attend !== undefined && r.attend === c.maPartie
  const erreur = (e: unknown) => {
    const probleme = lireProbleme(e)
    toast.error(probleme.detail ?? probleme.titre)
    queryClient.invalidateQueries({ queryKey: cleContrats })
  }
  const valider = useMutation({
    mutationFn: () => api.validerRetiraison(c.id, r.numero, r.version),
    onSuccess: (contrat) => {
      toast.success(
        contrat.etat === 'EXECUTE'
          ? 'Retiraison validée : le contrat est exécuté.'
          : 'Retiraison validée : stocks estimés et DSM la reçoivent.'
      )
      queryClient.invalidateQueries({ queryKey: cleContrats })
      // RG-CA-21, 22 : ses mouvements entrent dans les stocks estimés et les DSM des deux parties.
      queryClient.invalidateQueries({ queryKey: ['declarations'] })
    },
    onError: erreur,
    meta: { erreurAffichee: true },
  })
  const retirer = useMutation({
    mutationFn: () => api.retirerRetiraison(c.id, r.numero),
    onSuccess: () => {
      setRetrait(false)
      toast.success('Retiraison retirée.')
      queryClient.invalidateQueries({ queryKey: cleContrats })
    },
    onError: erreur,
    meta: { erreurAffichee: true },
  })
  const nomAttendu =
    r.attend === 'VENDEUR' ? c.vendeur.raisonSociale : c.acheteur.raisonSociale
  return (
    <div
      className={cn(
        'bg-card space-y-2 rounded-md border p-3',
        aMoi && 'border-warning-border'
      )}
    >
      <div className='flex flex-wrap items-center justify-between gap-2'>
        <span className='flex flex-wrap items-center gap-2 font-medium'>
          <Truck className='text-muted-foreground size-4' />
          Retiraison n° {r.numero}
          <BadgePlein statut={ETATS_RETIRAISON[r.etat]}>
            {r.etat === 'VALIDEE'
              ? 'Validée'
              : aMoi
                ? 'À valider par vous'
                : r.libelleEtat}
          </BadgePlein>
        </span>
        <span className='text-sm'>
          Enlèvement le{' '}
          <Surligne changement={champ('dateEnlevement')}>
            {formatDate(r.dateEnlevement)}
          </Surligne>
          {(r.derniere || champ('derniere')) && (
            <>
              {' · '}
              <Surligne changement={champ('derniere')}>
                {r.derniere ? 'dernière' : 'pas la dernière'}
              </Surligne>
            </>
          )}
        </span>
      </div>
      <ul className='space-y-0.5 text-sm'>
        {r.lignes.map((l) => (
          <li key={l.rang} className='flex flex-wrap justify-between gap-x-3'>
            <span>{designation(c.version.lignes[l.rang])}</span>
            <span className='tabular-nums'>
              <Surligne changement={changementLigne(l.rang)}>
                {formatHl(l.volume)}
              </Surligne>{' '}
              <span className='text-muted-foreground'>
                ({formatHlAp(l.volumeAp)})
              </span>
            </span>
          </li>
        ))}
        {r.ecart?.lignes
          .filter((x) => x.nature === 'RETIREE')
          .map((x) => (
            <li
              key={`retiree-${x.rang}`}
              className='text-muted-foreground flex justify-between gap-x-3'
            >
              <del>{designation(c.version.lignes[x.rang])}</del>
              <span>ligne retirée</span>
            </li>
          ))}
      </ul>
      <p className='text-muted-foreground text-xs'>
        Version {r.version}, par {r.auteur.nom} (
        {PARTIES[r.auteur.partie].toLowerCase()}) le{' '}
        {formatDateHeure(r.auteur.le)}
        {r.attend && !aMoi && ` · attend ${nomAttendu}`}
        {r.ecart && ` · modifiée depuis la version ${r.ecart.depuis}`}
      </p>
      {r.stockInsuffisant && (
        <Alert variant='destructive'>
          <AlertTriangle />
          <AlertTitle>Vous ne pouvez pas valider cette retiraison</AlertTitle>
          <AlertDescription>
            <p>{r.stockInsuffisant}</p>
            <p>Modifiez-la pour la ramener à votre stock.</p>
          </AlertDescription>
        </Alert>
      )}
      {agit && r.etat !== 'VALIDEE' && (aMoi || c.maPartie === 'ACHETEUR') && (
        <div className='flex flex-wrap gap-2'>
          {aMoi && (
            <Button
              size='sm'
              onClick={() => valider.mutate()}
              disabled={valider.isPending || Boolean(r.stockInsuffisant)}
              className='max-md:flex-1'
            >
              <Check /> Valider
            </Button>
          )}
          {aMoi && (
            <Button
              size='sm'
              variant='outline'
              onClick={onModifier}
              className='max-md:flex-1'
            >
              <Pencil /> Modifier
            </Button>
          )}
          {c.maPartie === 'ACHETEUR' && (
            <Button
              size='sm'
              variant='outline'
              className='text-destructive max-md:flex-1'
              onClick={() => setRetrait(true)}
            >
              <X /> Retirer
            </Button>
          )}
        </div>
      )}
      <ConfirmDialog
        open={retrait}
        onOpenChange={setRetrait}
        title={`Retirer la retiraison n° ${r.numero} ?`}
        desc='Elle est supprimée ; le journal du contrat garde sa trace.'
        confirmText='Retirer'
        destructive
        isLoading={retirer.isPending}
        handleConfirm={() => retirer.mutate()}
      />
    </div>
  )
}

const aujourdhui = () => new Date().toLocaleDateString('sv-SE')

/**
 * Déclarer une retiraison (l'acheteur) ou la modifier (la partie attendue) : la date d'enlèvement, au plus tôt
 * aujourd'hui, « dernière » ou non, et le volume retiré de chaque ligne, au plus son reste (RG-CA-13, 14). Le contrôle
 * du stock estimé du vendeur se fait à l'enregistrement.
 */
export function FormulaireRetiraison({
  operateur,
  contrat: c,
  retiraison,
  onFermer,
}: {
  operateur: number
  contrat: Contrat
  retiraison?: RetiraisonContrat
  onFermer: () => void
}) {
  const queryClient = useQueryClient()
  const api = apiContrats(operateur)
  const [date, setDate] = useState(retiraison?.dateEnlevement ?? aujourdhui())
  const [derniere, setDerniere] = useState(retiraison?.derniere ?? false)
  const [volumes, setVolumes] = useState<Record<number, string>>(() =>
    Object.fromEntries(
      (retiraison?.lignes ?? []).map((l) => [
        l.rang,
        String(l.volume).replace('.', ','),
      ])
    )
  )
  const [probleme, setProbleme] = useState<string>()
  /** Le reste d'une ligne, en rendant à la retiraison modifiée ce qu'elle retirait. */
  const reste = (rang: number) =>
    c.restes[rang].reste +
    (retiraison?.lignes.find((l) => l.rang === rang)?.volume ?? 0)
  const lignes = c.version.lignes
    .map((_, rang) => ({ rang, volume: lireNombre(volumes[rang] ?? '') }))
    .filter((l) => l.volume !== null && l.volume > 0)
  const depasse = lignes.some((l) => l.volume! > reste(l.rang) + 1e-9)
  const enregistrer = useMutation({
    mutationFn: (corps: SaisieRetiraison) =>
      retiraison
        ? api.modifierRetiraison(
            c.id,
            retiraison.numero,
            retiraison.version,
            corps
          )
        : api.declarerRetiraison(c.id, corps),
    onSuccess: () => {
      toast.success(
        retiraison
          ? "Retiraison modifiée : elle attend l'autre partie."
          : 'Retiraison déclarée : elle attend le vendeur.'
      )
      queryClient.invalidateQueries({ queryKey: cleContrats })
      onFermer()
    },
    onError: (e) => {
      const p = lireProbleme(e)
      setProbleme(p.detail ?? p.titre)
    },
    meta: { erreurAffichee: true },
  })
  const envoyer = () => {
    setProbleme(undefined)
    enregistrer.mutate({
      dateEnlevement: date,
      derniere,
      lignes: lignes.map((l) => ({ rang: l.rang, volume: l.volume! })),
    })
  }
  return (
    <DialogueFormulaire
      ouvert
      onOuvertChange={(o) => !o && onFermer()}
      titre={
        retiraison
          ? `Modifier la retiraison n° ${retiraison.numero}`
          : 'Déclarer une retiraison'
      }
      description={`Contrat ${c.numero}. ${
        retiraison
          ? "Votre version l'emporte : l'autre partie la validera."
          : 'Le vendeur la validera, au plus tard le jour de l’enlèvement.'
      }`}
      className='sm:max-w-lg'
    >
      <div className='space-y-1'>
        <Label htmlFor='date-enlevement'>Date d'enlèvement</Label>
        <Input
          id='date-enlevement'
          type='date'
          min={aujourdhui()}
          value={date}
          onChange={(e) => setDate(e.target.value)}
        />
      </div>
      {c.version.lignes.map((l, rang) => {
        const saisi = lireNombre(volumes[rang] ?? '')
        const max = reste(rang)
        const invalide =
          (volumes[rang] ?? '') !== '' && (saisi === null || saisi > max + 1e-9)
        if (max <= 0) return null
        return (
          <div key={rang} className='space-y-1'>
            <Label htmlFor={`volume-${rang}`}>
              {designation(l)} à {formatDegre(l.degre)}
            </Label>
            <div className='relative'>
              <Input
                id={`volume-${rang}`}
                inputMode='decimal'
                placeholder='0'
                value={volumes[rang] ?? ''}
                aria-invalid={invalide}
                onChange={(e) =>
                  setVolumes({ ...volumes, [rang]: e.target.value })
                }
                className='pe-10 text-right tabular-nums'
              />
              <span className='text-muted-foreground pointer-events-none absolute inset-y-0 end-3 flex items-center text-xs'>
                hl
              </span>
            </div>
            <p
              className={cn(
                'text-xs',
                invalide ? 'text-destructive' : 'text-muted-foreground'
              )}
            >
              Reste {formatHl(max)} à retirer
              {saisi !== null &&
                saisi > 0 &&
                `, soit ${formatHlAp(volumeAp(saisi, l.degre))} pour ${formatHl(saisi)}`}
              .
            </p>
          </div>
        )
      })}
      <div className='flex items-start gap-2'>
        <Checkbox
          id='derniere'
          checked={derniere}
          onCheckedChange={(v) => setDerniere(v === true)}
        />
        <Label htmlFor='derniere' className='leading-snug font-normal'>
          Dernière retiraison : sa validation clôt le contrat (exécuté), même
          s'il reste du volume.
        </Label>
      </div>
      {probleme && (
        <Alert variant='destructive'>
          <AlertTriangle />
          <AlertTitle>Retiraison refusée</AlertTitle>
          <AlertDescription>{probleme}</AlertDescription>
        </Alert>
      )}
      <div className='flex flex-wrap justify-end gap-2'>
        <Button variant='outline' onClick={onFermer} className='max-md:hidden'>
          Annuler
        </Button>
        <Button
          onClick={envoyer}
          disabled={
            enregistrer.isPending ||
            lignes.length === 0 ||
            depasse ||
            date < aujourdhui()
          }
          className='max-md:w-full'
        >
          <Check /> {retiraison ? 'Enregistrer' : 'Déclarer'}
        </Button>
      </div>
    </DialogueFormulaire>
  )
}
