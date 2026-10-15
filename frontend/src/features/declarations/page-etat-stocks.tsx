import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { PageMetier } from '@/components/metier/page-metier'
import { useMoi } from '@/features/session/session'
import { useStockDeclare } from './api'
import { ResumeStockDeclare } from './stock-declare'

/**
 * Portail, « Stocks déclarés » (spec 05 §6.2) : le stock déclaré de l'opérateur actif (RG-DEC-42), stock de fin de
 * sa dernière DSM validée, ligne par ligne ; lu par tout lecteur de ses déclarations (RG-DEC-01). Pendant du menu
 * « Stocks déclarés » du back-office, réduit à l'opérateur.
 */
export function PageEtatStocks() {
  const { actif } = useMoi()
  const operateur = actif?.operateur ?? 0
  const stock = useStockDeclare(operateur)
  return (
    <PageMetier
      titre='Stocks déclarés'
      description='Le stock déclaré de votre entreprise : le stock de fin de votre dernière DSM validée.'
    >
      <Card className='mb-8'>
        <CardHeader>
          <CardTitle className='text-base'>Dernière DSM validée</CardTitle>
        </CardHeader>
        <CardContent>
          {stock.isPending && (
            <p className='text-muted-foreground text-sm'>Chargement…</p>
          )}
          {stock.isSuccess && <ResumeStockDeclare stock={stock.data} />}
        </CardContent>
      </Card>
    </PageMetier>
  )
}
