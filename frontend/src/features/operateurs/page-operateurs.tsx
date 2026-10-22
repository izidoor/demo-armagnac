import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import {
  ChevronRight,
  Download,
  Mail,
  Phone,
  Plus,
  Search,
  TriangleAlert,
} from 'lucide-react'
import { toast } from 'sonner'
import { exporterOperateurs, listerOperateurs } from '@/api/generated'
import { useFiltresAdresse, useTexteAdresse } from '@/hooks/use-filtres-adresse'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/components/ui/tooltip'
import { BadgeEtat } from '@/components/metier/badge-etat'
import { CarteListe, CartesListe } from '@/components/metier/cartes-liste'
import { LignesEtat } from '@/components/metier/lignes-etat'
import { PageMetier } from '@/components/metier/page-metier'
import { useDroits, useMoi } from '@/features/session/session'
import { TYPES, TYPES_CHOISIS, type TypeOperateur } from './commun/libelles'
import { CreationOperateur } from './creation-operateur'

const TOUS = 'tous'

/** Avertissement léger : la fiche manque d'une donnée pour facturer ou certifier (RG-OPE-02). */

/**
 * Listing du back-office (§4.3.1 p.18, spec 02 §6.1) : recherche unique et filtres, ouverture de la fiche, export
 * Excel (RG-OPE-38). Le type et la recherche peuvent venir de l'URL (accès préfiltré du tableau de bord, §4.1).
 * Selon la largeur du contenu (`CartesListe`) : sous 58rem, une carte par opérateur à la place du tableau (847 px de
 * large naturelle sans code postal ni ville) ; sous 82rem (1256 px avec), le tableau perd le code postal et la ville,
 * et ses badges « Non facturable », « Non certifiable » se résument en une icône (`Manques`) : une ligne par
 * opérateur.
 */
export function PageOperateurs() {
  const navigate = useNavigate()
  const [filtres, changer] = useFiltresAdresse({
    type: TOUS as string,
    etat: 'actifs',
    completude: TOUS as string,
    recherche: '',
  })
  const { type, etat, completude } = filtres
  const [recherche, setRecherche, rechercheStable] = useTexteAdresse(
    filtres.recherche,
    (texte) => changer({ recherche: texte })
  )
  const [creation, setCreation] = useState(false)
  const { actif } = useMoi()
  // RG-OPE-32 : créer un opérateur demande la gestion du module Contacts dans l'opérateur BNIA (l'actif).
  const creationPermise =
    useDroits(actif?.operateur ?? 0)?.contacts === 'GESTION'

  const filtre = {
    type: type === TOUS ? undefined : (type as TypeOperateur),
    recherche: rechercheStable.trim() || undefined,
    actif: etat === TOUS ? undefined : etat === 'actifs',
    // RG-OPE-02 : fiches facturables ou certifiables, ou celles auxquelles il manque quelque chose.
    facturable:
      completude === 'non-facturables'
        ? false
        : completude === 'completes'
          ? true
          : undefined,
    certifiable:
      completude === 'non-certifiables'
        ? false
        : completude === 'completes'
          ? true
          : undefined,
  }
  const requete = useQuery({
    queryKey: ['operateurs', filtre],
    queryFn: () =>
      listerOperateurs({ query: filtre, throwOnError: true }).then(
        (r) => r.data
      ),
  })

  const exporter = async () => {
    try {
      const reponse = await exporterOperateurs({
        query: filtre,
        throwOnError: true,
      })
      const url = URL.createObjectURL(reponse.data as Blob)
      const lien = document.createElement('a')
      lien.href = url
      lien.download = `operateurs-${new Date().toISOString().slice(0, 10)}.xlsx`
      lien.click()
      URL.revokeObjectURL(url)
    } catch {
      toast.error("L'export n'a pas pu être produit.")
    }
  }

  const ouvrir = (id: number) =>
    navigate({ to: '/bo/operateurs/$id', params: { id: String(id) } })

  return (
    <PageMetier
      titre='Opérateurs'
      description='Producteurs, négociants et autres structures suivis par le BNIA.'
      actions={
        <div className='flex gap-2'>
          <Button variant='outline' onClick={exporter}>
            <Download /> Exporter
          </Button>
          {creationPermise && (
            <Button onClick={() => setCreation(true)}>
              <Plus /> Nouvel opérateur
            </Button>
          )}
        </div>
      }
    >
      <div className='mb-4 flex flex-wrap items-center gap-2'>
        <div className='relative w-full sm:w-auto'>
          <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
          <Input
            placeholder='Rechercher un opérateur'
            title='Code, raison sociale, nom commercial, code postal, ville'
            aria-label='Recherche par code, raison sociale, nom commercial, code postal ou ville'
            value={recherche}
            onChange={(e) => setRecherche(e.target.value)}
            className='h-9 w-full ps-8 sm:w-96'
          />
        </div>
        <Select value={type} onValueChange={(v) => changer({ type: v })}>
          <SelectTrigger className='h-9 w-36'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TOUS}>Tous les types</SelectItem>
            {Object.entries(TYPES_CHOISIS).map(([valeur, libelle]) => (
              <SelectItem key={valeur} value={valeur}>
                {libelle}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={etat} onValueChange={(v) => changer({ etat: v })}>
          <SelectTrigger className='h-9 w-32'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='actifs'>Actifs</SelectItem>
            <SelectItem value='inactifs'>Inactifs</SelectItem>
            <SelectItem value={TOUS}>Tous</SelectItem>
          </SelectContent>
        </Select>
        <Select
          value={completude}
          onValueChange={(v) => changer({ completude: v })}
        >
          <SelectTrigger className='h-9 w-44'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value={TOUS}>Complètes ou non</SelectItem>
            <SelectItem value='non-facturables'>Non facturables</SelectItem>
            <SelectItem value='non-certifiables'>Non certifiables</SelectItem>
            <SelectItem value='completes'>
              Facturables et certifiables
            </SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className='rounded-md border @max-[58rem]/content:hidden'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Code</TableHead>
              <TableHead>Raison sociale</TableHead>
              <TableHead>Type</TableHead>
              {/* Contenu sous 82rem : sans code postal ni ville, une ligne par opérateur tient dans la page. */}
              <TableHead className='@max-[82rem]/content:hidden'>
                Code postal
              </TableHead>
              <TableHead className='@max-[82rem]/content:hidden'>
                Ville
              </TableHead>
              <TableHead>Téléphone</TableHead>
              <TableHead>E-mail</TableHead>
              <TableHead>État</TableHead>
              <TableHead className='w-12' />
            </TableRow>
          </TableHeader>
          <TableBody>
            <LignesEtat requete={requete} colonnes={9} />
            {requete.data?.map((o) => (
              <TableRow
                key={o.id}
                className='cursor-pointer'
                onClick={() => ouvrir(o.id)}
              >
                <TableCell className='font-mono'>{o.code}</TableCell>
                <TableCell>
                  <span className='flex items-center gap-2'>
                    {o.raisonSociale}
                    <Manques
                      facturable={o.facturable}
                      certifiable={o.certifiable}
                    />
                  </span>
                </TableCell>
                <TableCell>
                  <Badge variant='secondary'>{TYPES[o.type]}</Badge>
                </TableCell>
                <TableCell className='@max-[82rem]/content:hidden'>
                  {o.codePostal}
                </TableCell>
                <TableCell className='@max-[82rem]/content:hidden'>
                  {o.ville}
                </TableCell>
                <TableCell>{o.telephone1}</TableCell>
                <TableCell>{o.emailGeneral}</TableCell>
                <TableCell>
                  <BadgeEtat actif={o.actif} />
                </TableCell>
                <TableCell>
                  <Button
                    variant='ghost'
                    size='sm'
                    asChild
                    onClick={(e) => e.stopPropagation()}
                  >
                    <Link
                      to='/bo/operateurs/$id'
                      params={{ id: String(o.id) }}
                      aria-label={`Ouvrir la fiche de ${o.raisonSociale}`}
                    >
                      <ChevronRight className='size-4' />
                    </Link>
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
      <CartesListe requete={requete} masquee='@min-[58rem]/content:hidden'>
        {requete.data?.map((o) => (
          <CarteListe
            key={o.id}
            lien={{ to: '/bo/operateurs/$id', params: { id: String(o.id) } }}
            titre={o.raisonSociale}
          >
            <div className='text-muted-foreground flex flex-wrap items-center gap-x-2 gap-y-1 text-sm'>
              <span className='text-foreground font-mono'>{o.code}</span>
              <Badge variant='secondary'>{TYPES[o.type]}</Badge>
              <BadgeEtat actif={o.actif} />
              {(o.codePostal || o.ville) && (
                <span>{[o.codePostal, o.ville].filter(Boolean).join(' ')}</span>
              )}
            </div>
            {(o.telephone1 || o.emailGeneral) && (
              <div className='flex flex-wrap gap-x-4 gap-y-1 text-sm'>
                {o.telephone1 && (
                  <a
                    href={`tel:${o.telephone1.replace(/\s/g, '')}`}
                    className='relative z-10 inline-flex items-center gap-1.5 hover:underline'
                  >
                    <Phone className='text-muted-foreground size-3.5' />
                    {o.telephone1}
                  </a>
                )}
                {o.emailGeneral && (
                  <a
                    href={`mailto:${o.emailGeneral}`}
                    className='relative z-10 inline-flex min-w-0 items-center gap-1.5 hover:underline'
                  >
                    <Mail className='text-muted-foreground size-3.5 shrink-0' />
                    <span className='truncate'>{o.emailGeneral}</span>
                  </a>
                )}
              </div>
            )}
            {(!o.facturable || !o.certifiable) && (
              <div className='flex flex-wrap gap-1.5'>
                {!o.facturable && (
                  <Badge variant='warning'>Non facturable</Badge>
                )}
                {!o.certifiable && (
                  <Badge variant='warning'>Non certifiable</Badge>
                )}
              </div>
            )}
          </CarteListe>
        ))}
      </CartesListe>
      <CreationOperateur
        ouvert={creation}
        onOuvertChange={setCreation}
        onCree={ouvrir}
      />
    </PageMetier>
  )
}

/**
 * RG-OPE-02 : ce qui manque à la fiche, dans le tableau. Contenu dès 82rem, en badges ; en dessous, faute de place, une icône
 * d'avertissement, le détail en infobulle.
 */
function Manques({
  facturable,
  certifiable,
}: {
  facturable: boolean
  certifiable: boolean
}) {
  const manques = [
    !facturable && 'Non facturable',
    !certifiable && 'Non certifiable',
  ].filter((m): m is string => !!m)
  if (manques.length === 0) return null
  return (
    <>
      {manques.map((m) => (
        <Badge
          key={m}
          variant='warning'
          className='@max-[82rem]/content:hidden'
        >
          {m}
        </Badge>
      ))}
      <Tooltip>
        <TooltipTrigger asChild>
          <span
            tabIndex={0}
            aria-label={manques.join(', ')}
            className='text-warning-foreground @min-[82rem]/content:hidden'
          >
            <TriangleAlert className='size-4' />
          </span>
        </TooltipTrigger>
        <TooltipContent>{manques.join(', ')}</TooltipContent>
      </Tooltip>
    </>
  )
}
