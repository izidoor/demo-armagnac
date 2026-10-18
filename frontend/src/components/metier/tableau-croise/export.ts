import { type Cell, type Row } from 'write-excel-file/browser'
import { type Croisement, type Noeud } from './calcul'

const NOMBRE = '#,##0.00'

const nombre = (v: number | null | undefined): Cell =>
  v === null || v === undefined
    ? null
    : { value: v, type: Number, format: NOMBRE }

/**
 * Le tableau croisé tel qu'il est composé, en Excel : tous les niveaux dépliés, en retrait, sous-totaux en
 * gras, totaux quand ils se cumulent. Écrit dans le navigateur.
 */
export async function exporterExcel(
  croisement: Croisement,
  titreLignes: string,
  nomFichier: string
) {
  const { default: ecrire } = await import('write-excel-file/browser')
  const entete: Row = [
    titreLignes,
    ...croisement.colonnes.map((c) => c.libelle),
    ...(croisement.totalColonnes ? ['Total'] : []),
  ].map((v) => ({ value: v, fontWeight: 'bold' as const }))
  const rangees: Row[] = [entete]
  const parcourir = (noeuds: Noeud[], niveau: number) => {
    for (const n of noeuds) {
      const gras = n.subRows ? ('bold' as const) : undefined
      rangees.push([
        { value: n.libelle, indent: niveau, fontWeight: gras },
        ...croisement.colonnes.map((c) => nombre(n.valeurs[c.cle])),
        ...(croisement.totalColonnes ? [nombre(n.total)] : []),
      ])
      if (n.subRows) parcourir(n.subRows, niveau + 1)
    }
  }
  parcourir(croisement.lignes, 0)
  if (croisement.totaux) {
    const totaux = croisement.totaux
    rangees.push([
      { value: 'Total', fontWeight: 'bold' },
      ...croisement.colonnes.map((c) => nombre(totaux.valeurs[c.cle])),
      ...(croisement.totalColonnes ? [nombre(totaux.total)] : []),
    ])
  }
  await ecrire(rangees, {
    columns: [
      { width: 40 },
      ...croisement.colonnes.map(() => ({ width: 16 })),
      ...(croisement.totalColonnes ? [{ width: 16 }] : []),
    ],
    stickyRowsCount: 1,
  }).toFile(nomFichier)
}
