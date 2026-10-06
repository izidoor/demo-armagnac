import { ArrowLeftRight, LayoutDashboard } from 'lucide-react'
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
      title: 'Espaces',
      items: [{ title: 'Back-office BNIA', url: '/bo', icon: ArrowLeftRight }],
    },
  ],
}
