import {
  ArrowLeftRight,
  Building2,
  Globe,
  LayoutDashboard,
  Percent,
  Tags,
  Users,
} from 'lucide-react'
import { type Espace } from '../types'

export const espaceBackOffice: Espace = {
  nom: 'Back-office BNIA',
  description: 'Gestion interne',
  url: '/bo',
  user: {
    name: 'Agent BNIA',
    email: 'agent@armagnac.fr',
    avatar: '',
  },
  navGroups: [
    {
      title: 'Général',
      items: [{ title: 'Accueil', url: '/bo', icon: LayoutDashboard }],
    },
    {
      // Opérateurs (spec 02, §6.1). Droit « Gestion des opérateurs : Lecture, Écriture » appliqué avec
      // le contexte acces ; d'ici là, tout utilisateur du back-office administre.
      title: 'Opérateurs',
      items: [{ title: 'Opérateurs', url: '/bo/operateurs', icon: Building2 }],
    },
    {
      // Référentiels (spec 01, §6.1). Droits « Référentiels métier : Gérer » et « Taux de TVA :
      // Gérer » appliqués avec le contexte acces ; d'ici là, tout utilisateur du back-office administre.
      title: 'Paramétrage',
      items: [
        {
          title: 'Géographie',
          icon: Globe,
          items: [
            {
              title: 'Zones commerciales',
              url: '/bo/parametrage/zones-commerciales',
            },
            { title: 'Pays', url: '/bo/parametrage/pays' },
            { title: 'Régions', url: '/bo/parametrage/regions' },
          ],
        },
        {
          title: 'Qualification',
          icon: Users,
          items: [
            { title: 'Titres', url: '/bo/parametrage/titres' },
            { title: 'Labels', url: '/bo/parametrage/labels' },
            { title: 'Types de visite', url: '/bo/parametrage/types-visite' },
            { title: 'Langues', url: '/bo/parametrage/langues' },
            {
              title: "Centres d'intérêt",
              url: '/bo/parametrage/centres-interet',
            },
            { title: 'Membres BNIA', url: '/bo/parametrage/membres-bnia' },
          ],
        },
        {
          title: 'Présentation et commercialisation',
          icon: Tags,
          items: [
            { title: 'Marques', url: '/bo/parametrage/marques' },
            {
              title: 'Circuits de distribution',
              url: '/bo/parametrage/circuits-distribution',
            },
            {
              title: 'Produits commercialisés',
              url: '/bo/parametrage/produits-commercialises',
            },
            { title: 'Contenances', url: '/bo/parametrage/contenances' },
          ],
        },
        { title: 'TVA', url: '/bo/parametrage/classes-tva', icon: Percent },
      ],
    },
    {
      title: 'Espaces',
      items: [
        { title: 'Portail opérateur', url: '/portail', icon: ArrowLeftRight },
      ],
    },
  ],
}

export const espacePortail: Espace = {
  nom: 'Portail opérateur',
  description: 'Espace des opérateurs',
  url: '/portail',
  user: {
    name: 'Opérateur',
    email: 'operateur@example.fr',
    avatar: '',
  },
  navGroups: [
    {
      title: 'Général',
      items: [{ title: 'Accueil', url: '/portail', icon: LayoutDashboard }],
    },
    {
      // Spec 02, §6.2 : l'opérateur de démonstration tant que le contexte acces n'existe pas.
      title: 'Mon entreprise',
      items: [
        {
          title: 'Mon entreprise',
          url: '/portail/entreprise',
          icon: Building2,
        },
        { title: 'Contacts', url: '/portail/contacts', icon: Users },
      ],
    },
    {
      title: 'Espaces',
      items: [{ title: 'Back-office BNIA', url: '/bo', icon: ArrowLeftRight }],
    },
  ],
}
