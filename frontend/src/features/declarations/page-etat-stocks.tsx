import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from '@tanstack/react-router'
import { historiqueStockDeclare, type LigneStock } from '@/api/generated'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { PageMetier } from '@/components/metier/page-metier'
import { TableDonnees } from '@/components/metier/table-donnees'
import { useMoi } from '@/features/session/session'
import { cleDeclarations, useStockDeclare } from './api'
import { colonnesLigneStock } from './colonnes-stocks'
import { formatDate, formatDateHeure, formatLitres } from './format'
import { ChoixAnnee, TableauStocks } from './tableau-stocks'

/**
 * Portail, « Stocks déclarés » (spec 05 §6.2), lu par tout lecteur des déclarations de l'opérateur actif
 * (RG-DEC-01), en deux onglets indépendants : *Données*, le stock de fin de sa dernière DSM validée, ligne par
 * ligne (RG-DEC-42) ; *Analyse*, le tableau croisé dynamique et le graphique d'une année déclarative, stock de
 * sa dernière DSM validée à chaque fin de mois (RG-DEC-44). Pendant du menu du back-office, réduit à
 * l'opérateur.
 */
export function PageEtatStocks() {
  const { actif } = useMoi()
  if (!actif) return null
  return <EtatStocks key={actif.operateur} operateur={actif.operateur} />
}

function EtatStocks({ operateur }: { operateur: number }) {
  return (
    <PageMetier
      titre='Stocks déclarés'
      description='Le stock déclaré de votre entreprise : le stock de fin de vos DSM validées.'
    >
      <Tabs defaultValue='donnees' className='mb-8'>
        <TabsList>
          <TabsTrigger value='donnees'>Données</TabsTrigger>
          <TabsTrigger value='analyse'>Analyse</TabsTrigger>
        </TabsList>
        <TabsContent value='donnees' className='mt-4'>
          <DonneesStock operateur={operateur} />
        </TabsContent>
        <TabsContent value='analyse' className='mt-4'>
          <AnalyseStock operateur={operateur} />
        </TabsContent>
      </Tabs>
    </PageMetier>
  )
}

const COLONNES = colonnesLigneStock<LigneStock>()

/**
 * *Données* : le stock de fin de la dernière DSM validée, ligne par ligne, lignes nulles omises (RG-DEC-42) ;
 * recherche, tri et filtre par valeurs sur chaque colonne, total des lignes affichées.
 */
function DonneesStock({ operateur }: { operateur: number }) {
  const stock = useStockDeclare(operateur)
  if (stock.isSuccess && !stock.data) {
    return (
      <p className='text-muted-foreground text-sm'>
        Aucune DSM validée : pas encore de stock déclaré.
      </p>
    )
  }
  return (
    <>
      {stock.data && (
        <p className='mb-3 text-sm'>
          Au <strong>{formatDate(stock.data.periode.fin)}</strong>, fin de la{' '}
          <Link
            to='/portail/declarations/$dsm'
            params={{ dsm: String(stock.data.dsm) }}
            className='underline underline-offset-4'
          >
            DSM de {stock.data.periode.libelle}
          </Link>
          , validée le {formatDateHeure(stock.data.valideeLe)}.
        </p>
      )}
      <TableDonnees
        cle='stocks-declares-portail'
        donnees={stock.data?.lignes ?? []}
        colonnes={COLONNES}
        chiffrees={['stock']}
        chargement={stock.isPending}
        vide='Toutes les lignes sont finies à 0 : stock nul.'
        total={(l) => l.stock}
        formatTotal={formatLitres}
      />
    </>
  )
}

/** *Analyse* : tableau croisé et graphique d'une année déclarative (RG-DEC-44). */
function AnalyseStock({ operateur }: { operateur: number }) {
  const [annee, setAnnee] = useState<number>()
  const historique = useQuery({
    queryKey: [...cleDeclarations(operateur), 'stocks', annee],
    queryFn: () =>
      historiqueStockDeclare({
        path: { operateur },
        query: { annee },
        throwOnError: true,
      }).then((r) => r.data),
  })
  return (
    <>
      <div className='mb-4'>
        <ChoixAnnee
          historique={historique.data}
          valeur={annee}
          onChange={setAnnee}
        />
      </div>
      <Card>
        <CardHeader>
          <CardTitle className='text-base'>Tableau croisé dynamique</CardTitle>
        </CardHeader>
        <CardContent>
          {historique.isPending && (
            <p className='text-muted-foreground text-sm'>Chargement…</p>
          )}
          {historique.isSuccess && (
            <TableauStocks
              historique={historique.data}
              avecOperateur={false}
              cle='stocks-declares-portail'
            />
          )}
        </CardContent>
      </Card>
    </>
  )
}
