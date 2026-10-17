import '@tanstack/react-table'

declare module '@tanstack/react-table' {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  interface ColumnMeta<TData, TValue> {
    className?: string // apply to both th and td
    tdClassName?: string
    thClassName?: string
    /** Le titre de la colonne : son en-tête filtrable et le menu « Colonnes » de `TableDonnees`. */
    titre?: string
    /** La mise en forme d'un sous-total (ligne de groupe de `TableDonnees`). */
    format?: (valeur: number) => string
  }
}
