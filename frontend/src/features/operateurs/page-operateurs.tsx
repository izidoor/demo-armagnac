import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate } from '@tanstack/react-router'
import { ChevronRight, Download, Plus, Search } from 'lucide-react'
import { toast } from 'sonner'
import { exporterOperateurs, listerOperateurs } from '@/api/generated'
import { useDebounce } from '@/hooks/use-debounce'
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
import { BadgeEtat } from '@/components/metier/badge-etat'
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
 */
export function PageOperateurs({
  type: typeInitial,
  recherche: rechercheInitiale,
}: {
  type?: TypeOperateur
  recherche?: string
}) {
  const navigate = useNavigate()
  const [recherche, setRecherche] = useState(rechercheInitiale ?? '')
  const [type, setType] = useState<string>(typeInitial ?? TOUS)
  const [etat, setEtat] = useState('actifs')
  const [completude, setCompletude] = useState(TOUS)
  const [creation, setCreation] = useState(false)
  const { actif } = useMoi()
  // RG-OPE-32 : créer un opérateur demande la gestion du module Contacts dans l'opérateur BNIA (l'actif).
  const creationPermise =
    useDroits(actif?.operateur ?? 0)?.contacts === 'GESTION'
  const rechercheStable = useDebounce(recherche)

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
      description='Producteurs, négociants et autres structures suivis par le BNIA (§4.3).'
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
        <div className='relative'>
          <Search className='text-muted-foreground absolute start-2.5 top-2.5 size-4' />
          <Input
            placeholder='Rechercher un opérateur'
            title='Code, raison sociale, nom commercial, code postal, ville'
            aria-label='Recherche par code, raison sociale, nom commercial, code postal ou ville'
            value={recherche}
            onChange={(e) => setRecherche(e.target.value)}
            className='h-9 w-96 ps-8'
          />
        </div>
        <Select value={type} onValueChange={setType}>
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
        <Select value={etat} onValueChange={setEtat}>
          <SelectTrigger className='h-9 w-32'>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value='actifs'>Actifs</SelectItem>
            <SelectItem value='inactifs'>Inactifs</SelectItem>
            <SelectItem value={TOUS}>Tous</SelectItem>
          </SelectContent>
        </Select>
        <Select value={completude} onValueChange={setCompletude}>
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
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Code</TableHead>
              <TableHead>Raison sociale</TableHead>
              <TableHead>Type</TableHead>
              <TableHead>Code postal</TableHead>
              <TableHead>Ville</TableHead>
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
                  {o.raisonSociale}
                  {!o.facturable && (
                    <Badge variant='warning' className='ms-2'>
                      Non facturable
                    </Badge>
                  )}
                  {!o.certifiable && (
                    <Badge variant='warning' className='ms-2'>
                      Non certifiable
                    </Badge>
                  )}
                </TableCell>
                <TableCell>
                  <Badge variant='secondary'>{TYPES[o.type]}</Badge>
                </TableCell>
                <TableCell>{o.codePostal}</TableCell>
                <TableCell>{o.ville}</TableCell>
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
      <CreationOperateur
        ouvert={creation}
        onOuvertChange={setCreation}
        onCree={ouvrir}
      />
    </PageMetier>
  )
}
