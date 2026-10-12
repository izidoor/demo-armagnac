import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from '@tanstack/react-router'
import { Download, EllipsisVertical, Plus } from 'lucide-react'
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
import { TYPES, TYPES_CHOISIS, type TypeOperateur } from './commun/libelles'
import { CreationOperateur } from './creation-operateur'

const TOUS = 'tous'

/** Listing du back-office (§4.3.1 p.18, spec 02 §6.1) : recherche unique et filtres, ouverture de la fiche, export Excel (RG-OPE-38). */
export function PageOperateurs() {
  const navigate = useNavigate()
  const [recherche, setRecherche] = useState('')
  const [type, setType] = useState<string>(TOUS)
  const [etat, setEtat] = useState('actifs')
  const [completude, setCompletude] = useState(TOUS)
  const [creation, setCreation] = useState(false)
  const rechercheStable = useDebounce(recherche)

  const filtre = {
    type: type === TOUS ? undefined : (type as TypeOperateur),
    recherche: rechercheStable.trim() || undefined,
    actif: etat === TOUS ? undefined : etat === 'actifs',
    incomplet: completude === TOUS ? undefined : completude === 'incompletes',
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
          <Button onClick={() => setCreation(true)}>
            <Plus /> Nouvel opérateur
          </Button>
        </div>
      }
    >
      <div className='mb-4 flex flex-wrap items-center gap-2'>
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
        <Input
          placeholder='Code, raison sociale, nom commercial, code postal, ville'
          value={recherche}
          onChange={(e) => setRecherche(e.target.value)}
          className='h-9 w-96'
        />
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
            <SelectItem value='incompletes'>Fiches incomplètes</SelectItem>
            <SelectItem value='completes'>Fiches complètes</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <div className='rounded-md border'>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Code</TableHead>
              <TableHead>Raison sociale</TableHead>
              <TableHead>Nom commercial</TableHead>
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
            <LignesEtat requete={requete} colonnes={10} />
            {requete.data?.map((o) => (
              <TableRow
                key={o.id}
                className='cursor-pointer'
                onClick={() => ouvrir(o.id)}
              >
                <TableCell className='font-mono'>{o.code}</TableCell>
                <TableCell>
                  {o.raisonSociale}
                  {o.incomplet && (
                    <Badge variant='outline' className='ms-2'>
                      Incomplète
                    </Badge>
                  )}
                </TableCell>
                <TableCell>{o.nomCommercial}</TableCell>
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
                    size='icon'
                    className='size-8'
                    aria-label='Ouvrir la fiche'
                    title='Ouvrir la fiche'
                    onClick={(e) => {
                      e.stopPropagation()
                      ouvrir(o.id)
                    }}
                  >
                    <EllipsisVertical />
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
