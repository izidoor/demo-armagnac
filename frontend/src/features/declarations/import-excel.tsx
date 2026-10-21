import { type ReactNode, useState } from 'react'
import { AxiosError } from 'axios'
import { useMutation } from '@tanstack/react-query'
import { AlertTriangle, FileWarning } from 'lucide-react'
import { toast } from 'sonner'
import { type ErreurImport } from '@/api/generated'
import { lireProbleme } from '@/lib/probleme'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'

/** RG-DEC-37 : les erreurs situées d'un import refusé, lues dans le corps de la réponse 422. */
function erreursImport(error: unknown): ErreurImport[] {
  if (error instanceof AxiosError) {
    const corps = error.response?.data as
      | { erreurs?: ErreurImport[] }
      | undefined
    return corps?.erreurs ?? []
  }
  return []
}

/**
 * Import du modèle Excel (spec 05 §5.1, H-DEC-15), tout ou rien : un fichier refusé montre chacune de ses
 * erreurs à sa place dans le classeur (onglet, ligne, colonne) et rien n'est importé (RG-DEC-37) ; un fichier
 * importé montre ses avertissements (RG-DEC-38). Le fichier n'est pas conservé.
 */
export function DialogueImportExcel({
  titre,
  description,
  champs,
  ouvert,
  onOuvertChange,
  importer,
  onImporte,
}: {
  titre: string
  description: ReactNode
  /** Champs propres à l'import, sous le fichier. */
  champs?: ReactNode
  ouvert: boolean
  onOuvertChange: (ouvert: boolean) => void
  importer: (fichier: File) => Promise<{ avertissements: string[] }>
  /** Après un import réussi, avant l'affichage des avertissements. */
  onImporte: () => void
}) {
  const [fichier, setFichier] = useState<File | null>(null)
  const [avertissements, setAvertissements] = useState<string[] | null>(null)
  const mutation = useMutation({
    mutationFn: (f: File) => importer(f),
    meta: { erreurAffichee: true },
    onSuccess: (resultat, f) => {
      onImporte()
      if (resultat.avertissements.length === 0) {
        toast.success(`Fichier « ${f.name} » importé.`)
        fermer()
      } else {
        setAvertissements(resultat.avertissements)
      }
    },
  })
  const fermer = () => {
    setFichier(null)
    setAvertissements(null)
    mutation.reset()
    onOuvertChange(false)
  }
  const erreurs = mutation.error ? erreursImport(mutation.error) : []
  const probleme = mutation.error ? lireProbleme(mutation.error) : null

  return (
    <Dialog open={ouvert} onOpenChange={(o) => !o && fermer()}>
      <DialogContent className='sm:max-w-3xl'>
        <DialogHeader>
          <DialogTitle>{titre}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        {avertissements ? (
          <Alert variant='warning'>
            <AlertTriangle className='size-4' />
            <AlertTitle>Fichier importé, avec des avertissements</AlertTitle>
            <AlertDescription>
              <ul className='list-disc space-y-1 ps-4'>
                {avertissements.map((a) => (
                  <li key={a}>{a}</li>
                ))}
              </ul>
            </AlertDescription>
          </Alert>
        ) : (
          <form
            id='import-excel'
            className='space-y-4'
            onSubmit={(e) => {
              e.preventDefault()
              if (fichier) mutation.mutate(fichier)
            }}
          >
            <div className='space-y-2'>
              <Label htmlFor='import-excel-fichier'>
                Fichier Excel (.xlsx)
              </Label>
              <Input
                id='import-excel-fichier'
                type='file'
                accept='.xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
                onChange={(e) => {
                  setFichier(e.target.files?.[0] ?? null)
                  mutation.reset()
                }}
              />
            </div>
            {champs}
            {probleme && (
              <Alert variant='destructive'>
                <FileWarning className='size-4' />
                <AlertTitle>{probleme.detail}</AlertTitle>
                {erreurs.length > 0 && (
                  <AlertDescription>
                    <div className='max-h-72 w-full overflow-auto rounded-md border'>
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Onglet</TableHead>
                            <TableHead className='text-end'>Ligne</TableHead>
                            <TableHead>Colonne</TableHead>
                            <TableHead>Erreur</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {erreurs.map((e, i) => (
                            <TableRow key={i}>
                              <TableCell className='whitespace-nowrap'>
                                {e.onglet}
                              </TableCell>
                              <TableCell className='text-end tabular-nums'>
                                {e.ligne}
                              </TableCell>
                              <TableCell className='font-mono'>
                                {e.colonne}
                              </TableCell>
                              <TableCell className='whitespace-normal'>
                                {e.message}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </div>
                  </AlertDescription>
                )}
              </Alert>
            )}
          </form>
        )}
        <DialogFooter>
          {avertissements ? (
            <Button onClick={fermer}>Fermer</Button>
          ) : (
            <>
              <Button variant='outline' onClick={fermer}>
                Annuler
              </Button>
              <Button
                type='submit'
                form='import-excel'
                disabled={!fichier || mutation.isPending}
              >
                Importer
              </Button>
            </>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
