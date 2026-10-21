import { type Dsm, type NoteInterneDsm } from '@/api/generated'
import { CarnetNotesInternes } from '@/components/metier/notes-internes'
import { apiDeclarations } from './api'
import { identite } from './libelles'

const SUR_LA_DSM = 'dsm'

/**
 * Carnet de notes internes d'une DSM (spec 15 §3.7, RG-DEC-24), au back-office seulement : distinct des messages
 * échangés avec l'opérateur, qui ne le voit jamais. Une note vise la DSM ou une ligne (note de contrôle, §4.4.5
 * p.26) ; son auteur la modifie ou la supprime ; écrite par qui gère les Déclarations au BNIA.
 */
export function SectionNotesInternes({
  dsm,
  notes,
  peutAnnoter,
  ligneCiblee,
  onMontrerLigne,
  onChange,
}: {
  dsm: Dsm
  notes: NoteInterneDsm[]
  peutAnnoter: boolean
  /** Les notes de cette ligne sont mises en évidence (pastille de la grille). */
  ligneCiblee: number | null
  onMontrerLigne: (ligne: number) => void
  onChange: () => void
}) {
  const api = apiDeclarations(dsm.operateur)

  const libelleLigne = (id: number) => {
    const l = dsm.lignes.find((x) => x.id === id)
    return l ? identite(l) : `ligne n° ${id}`
  }

  return (
    <CarnetNotesInternes
      description="Le carnet du BNIA sur cette DSM ou l'une de ses lignes : notes de contrôle, rapprochements, points à vérifier. L'opérateur ne le voit jamais ; pour lui écrire, utilisez les messages."
      notes={notes}
      peutAnnoter={peutAnnoter}
      cibles={[
        { valeur: SUR_LA_DSM, libelle: 'La DSM' },
        ...dsm.lignes
          .filter((l) => l.id != null)
          .map((l) => ({
            valeur: String(l.id),
            libelle: `Ligne ${identite(l)}`,
          })),
      ]}
      ecrire={(texte, cible) =>
        api.annoter(dsm.id, {
          ligne: cible === SUR_LA_DSM ? undefined : Number(cible),
          texte,
        })
      }
      modifier={(n, texte) => api.modifierNote(dsm.id, n.id, { texte })}
      supprimer={(n) => api.supprimerNote(dsm.id, n.id)}
      onChange={onChange}
      enEvidence={(n) => ligneCiblee != null && n.ligne === ligneCiblee}
      etiquette={(n) => (
        <>
          {n.ligne != null && (
            <button
              type='button'
              className='bg-warning text-warning-foreground hover:bg-warning-border rounded-full px-2 py-0.5 font-medium'
              onClick={() => onMontrerLigne(n.ligne!)}
            >
              Ligne {libelleLigne(n.ligne)}
            </button>
          )}
          {n.ligneSupprimee && (
            <span className='rounded-full border border-dashed px-2 py-0.5 line-through'>
              Ligne {n.ligneSupprimee}
            </span>
          )}
        </>
      )}
    />
  )
}
