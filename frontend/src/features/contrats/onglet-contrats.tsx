import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { useContratsBnia } from './api'
import { ListeContrats } from './liste-contrats'

/** Onglet « Contrats » de la fiche opérateur, au back-office (spec 06 §6.1) : ses contrats, acheteur ou vendeur. */
export function OngletContrats({ operateur }: { operateur: number }) {
  const contrats = useContratsBnia({ operateur })
  return (
    <Card>
      <CardHeader>
        <CardTitle>Contrats d'achat Armagnac</CardTitle>
      </CardHeader>
      <CardContent>
        <ListeContrats
          espace='bo'
          contrats={contrats.data ?? []}
          chargement={contrats.isPending}
        />
      </CardContent>
    </Card>
  )
}
