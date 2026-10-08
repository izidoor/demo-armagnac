import {
  Building2,
  Contact,
  Globe,
  History,
  Landmark,
  LayoutDashboard,
  Percent,
  Tags,
  UserCog,
  Users,
} from 'lucide-react'
import { type Espace } from '../types'

/**
 * Back-office : l'opérateur actif est le BNIA (RG-OPE-49). Tous ses rôles lisent ces menus (spec 02
 * §3.2.7) ; les actions suivent les droits, vérifiés par le serveur.
 */
export const espaceBackOffice: Espace = {
  nom: 'Back-office BNIA',
  description: 'Gestion interne',
  url: '/bo',
  navGroups: [
    {
      title: 'Général',
      items: [{ title: 'Accueil', url: '/bo', icon: LayoutDashboard }],
    },
    {
      // Spec 02, §6.1.
      title: 'Opérateurs',
      items: [
        { title: 'Opérateurs', url: '/bo/operateurs', icon: Building2 },
        { title: 'BNIA', url: '/bo/bnia', icon: Landmark },
      ],
    },
    {
      // Spec 02, §6.1 et spec 03, §6.2 : module Accès.
      title: 'Accès',
      items: [
        { title: 'Utilisateurs', url: '/bo/acces/utilisateurs', icon: UserCog },
        {
          title: 'Journal des connexions',
          url: '/bo/acces/connexions',
          icon: History,
        },
      ],
    },
    {
      // Référentiels (spec 01, §6.1), module Paramétrage : lu par tout le personnel du BNIA, géré par
      // ses agents et administrateurs.
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
  ],
}

/** Portail : l'opérateur actif est un autre opérateur (RG-OPE-49) ; tout membre lit ces menus. */
export const espacePortail: Espace = {
  nom: 'Portail opérateur',
  description: 'Espace des opérateurs',
  url: '/portail',
  navGroups: [
    {
      title: 'Général',
      items: [{ title: 'Accueil', url: '/portail', icon: LayoutDashboard }],
    },
    {
      // Spec 02, §6.2.
      title: 'Mon entreprise',
      items: [
        {
          title: 'Mon entreprise',
          url: '/portail/entreprise',
          icon: Building2,
        },
        { title: 'Annuaire', url: '/portail/annuaire', icon: Contact },
      ],
    },
  ],
}
