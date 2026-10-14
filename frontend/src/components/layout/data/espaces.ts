import {
  Building2,
  Contact,
  FileSpreadsheet,
  Warehouse,
  Globe,
  History,
  Landmark,
  LayoutDashboard,
  Percent,
  Receipt,
  Tags,
  UserCog,
  Users,
} from 'lucide-react'
import { type Espace, type NavGroup } from '../types'

/**
 * Référentiels (spec 01, §6.1), module Paramétrage : lu par tout le personnel du BNIA, géré par ses agents
 * et administrateurs. Hors de la barre latérale : rubriques de la page Paramétrage, ouverte par la roue
 * crantée de l'en-tête. Quatre sections, chacune enchaînant ses référentiels ; chaque référentiel mène à
 * sa section, pour la recherche.
 */
const GEOGRAPHIE = '/bo/parametrage/geographie'
const QUALIFICATION = '/bo/parametrage/qualification'
const COMMERCIALISATION = '/bo/parametrage/commercialisation'
const TVA = '/bo/parametrage/classes-tva'

export const parametrage: NavGroup = {
  title: 'Paramétrage',
  items: [
    {
      title: 'Codes géographiques',
      icon: Globe,
      items: [
        { title: 'Zones commerciales', url: GEOGRAPHIE },
        { title: 'Pays', url: GEOGRAPHIE },
        { title: 'Régions', url: GEOGRAPHIE },
      ],
    },
    {
      title: 'Qualification',
      icon: Users,
      items: [
        { title: 'Titres', url: QUALIFICATION },
        { title: 'Membres BNIA', url: QUALIFICATION },
        { title: "Centres d'intérêt", url: QUALIFICATION },
        { title: 'Langues', url: QUALIFICATION },
        { title: 'Types de visite', url: QUALIFICATION },
        { title: 'Labels', url: QUALIFICATION },
      ],
    },
    {
      title: 'Commercialisation',
      icon: Tags,
      items: [
        { title: 'Produits commercialisés', url: COMMERCIALISATION },
        { title: 'Contenances', url: COMMERCIALISATION },
        { title: 'Circuits de distribution', url: COMMERCIALISATION },
        { title: 'Marques', url: COMMERCIALISATION },
      ],
    },
    {
      title: 'TVA',
      icon: Percent,
      items: [{ title: 'Classes de TVA', url: TVA }],
    },
    {
      // Spec 05 §6.1 : la grille de la cotisation des DSM (RG-DEC-30).
      title: 'Cotisation',
      icon: Receipt,
      items: [{ title: 'Taux CVO', url: '/bo/parametrage/taux-cvo' }],
    },
  ],
}

/**
 * Back-office : l'opérateur actif est le BNIA (RG-OPE-49). Tous ses rôles lisent ces menus (spec 02
 * §3.2.7) ; les actions suivent les droits, vérifiés par le serveur.
 */
export const espaceBackOffice: Espace = {
  nom: 'Back-office BNIA',
  description: 'Gestion interne',
  url: '/bo',
  autresPages: [parametrage],
  navGroups: [
    {
      title: 'Général',
      items: [{ title: 'Accueil', url: '/bo', icon: LayoutDashboard }],
    },
    {
      // Spec 02, §6.1.
      title: 'Opérateurs',
      items: [
        { title: 'BNIA', url: '/bo/bnia', icon: Landmark },
        { title: 'Opérateurs', url: '/bo/operateurs', icon: Building2 },
      ],
    },
    {
      // Spec 05, §6.1 : listing de toutes les DSM, file d'attente de l'instruction ; stocks déclarés (lot 8).
      title: 'Déclarations',
      items: [
        { title: 'DSM', url: '/bo/declarations', icon: FileSpreadsheet },
        {
          title: 'Stocks déclarés',
          url: '/bo/declarations/stocks',
          icon: Warehouse,
        },
      ],
    },
    {
      // Spec 02, §6.1 et spec 03, §6.2 : module Accès.
      title: 'Accès Portail',
      items: [
        { title: 'Utilisateurs', url: '/bo/acces/utilisateurs', icon: UserCog },
        {
          title: 'Journal des connexions',
          url: '/bo/acces/connexions',
          icon: History,
        },
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
    {
      // Spec 05, §6.2.
      title: 'Déclarations',
      items: [
        {
          title: 'Mes DSM',
          url: '/portail/declarations',
          icon: FileSpreadsheet,
        },
      ],
    },
  ],
}
