import { Fragment, type ReactNode } from 'react'
import { Separator } from '@/components/ui/separator'
import { PageContenances } from '../contenances/page-contenances'
import {
  centresInteret,
  circuitsDistribution,
  labels,
  langues,
  marques,
  membresBnia,
  produitsCommercialises,
  titres,
  typesVisite,
  zonesCommerciales,
} from '../listes-simples/listes'
import { PageListeSimple } from '../listes-simples/page-liste-simple'
import { PagePays } from '../pays/page-pays'
import { PageRegions } from '../regions/page-regions'

/** Les référentiels d'une section, l'un sous l'autre, séparés par un trait horizontal. */
function Section({ children }: { children: ReactNode[] }) {
  return (
    <div className='pb-8'>
      {children.map((enfant, i) => (
        <Fragment key={i}>
          {i > 0 && <Separator className='my-8' />}
          <section>{enfant}</section>
        </Fragment>
      ))}
    </div>
  )
}

export function SectionGeographie() {
  return (
    <Section>
      {[
        <PageListeSimple key='zones' config={zonesCommerciales} />,
        <PagePays key='pays' />,
        <PageRegions key='regions' />,
      ]}
    </Section>
  )
}

export function SectionQualification() {
  return (
    <Section>
      {[titres, membresBnia, centresInteret, langues, typesVisite, labels].map(
        (config) => (
          <PageListeSimple key={config.titre} config={config} />
        )
      )}
    </Section>
  )
}

export function SectionCommercialisation() {
  return (
    <Section>
      {[
        <PageListeSimple key='produits' config={produitsCommercialises} />,
        <PageContenances key='contenances' />,
        <PageListeSimple key='circuits' config={circuitsDistribution} />,
        <PageListeSimple key='marques' config={marques} />,
      ]}
    </Section>
  )
}
