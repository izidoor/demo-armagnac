import {
  Building2,
  Contact,
  FileSpreadsheet,
  FileText,
  Warehouse,
  Globe,
  History,
  Landmark,
  LayoutDashboard,
  Mail,
  Package,
  Percent,
  Receipt,
  Tags,
  TrendingUp,
  UserCog,
  Users,
} from 'lucide-react'
import { type Espace, type NavGroup } from '../types'

/**
 * Paramètres de l'application (spec 01, §6.1) : le BNIA (sa fiche, ses contacts, le vendeur des factures),
 * puis les référentiels du module Paramétrage, lus par tout le personnel du BNIA, gérés par ses agents et
 * administrateurs. Hors de la barre latérale : sections de la page ouverte par la roue crantée de
 * l'en-tête, chacune enchaînant ses blocs ; chaque bloc mène à sa section, pour la recherche.
 */
const GEOGRAPHIE = '/bo/parametrage/geographie'
const QUALIFICATION = '/bo/parametrage/qualification'
const COMMERCIALISATION = '/bo/parametrage/commercialisation'
const TVA = '/bo/parametrage/classes-tva'
const BNIA = '/bo/parametrage/bnia'

export const parametrage: NavGroup = {
  title: "Paramètres de l'application",
  items: [
    {
      // Spec 02 §6.1 et spec 08 §6.1 : la fiche réduite du BNIA, ses contacts, le vendeur des factures.
      title: 'BNIA',
      icon: Landmark,
      items: [
        { title: 'Fiche', url: BNIA },
        { title: 'Contacts', url: BNIA },
        { title: 'Facturation', url: BNIA },
      ],
    },
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
      items: [
        { title: 'Accueil', url: '/bo', icon: LayoutDashboard },
        // Spec 15 §6.1 : les échanges avec les opérateurs.
        { title: 'Messagerie', url: '/bo/messagerie', icon: Mail },
      ],
    },
    {
      // Spec 02, §6.1.
      title: 'Opérateurs',
      items: [{ title: 'Opérateurs', url: '/bo/operateurs', icon: Building2 }],
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
      // Spec 08, §6.1 : historique des factures et brouillons à émettre, articles, ventes.
      title: 'Facturation',
      items: [
        { title: 'Factures', url: '/bo/facturation', icon: FileText },
        { title: 'Articles', url: '/bo/facturation/articles', icon: Package },
        { title: 'Ventes', url: '/bo/facturation/ventes', icon: TrendingUp },
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
      items: [
        { title: 'Accueil', url: '/portail', icon: LayoutDashboard },
        // Spec 15 §6.2 : les échanges avec le BNIA.
        { title: 'Messagerie', url: '/portail/messagerie', icon: Mail },
      ],
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
        {
          title: 'Mes utilisateurs',
          url: '/portail/annuaire',
          icon: Contact,
        },
      ],
    },
    {
      // Spec 05, §6.2 : les DSM de l'opérateur, puis son stock déclaré (RG-DEC-42).
      title: 'Déclarations',
      items: [
        {
          title: 'DSM',
          url: '/portail/declarations',
          icon: FileSpreadsheet,
        },
        {
          title: 'Stocks déclarés',
          url: '/portail/declarations/stocks',
          icon: Warehouse,
        },
      ],
    },
    {
      // Spec 08, §6.2 : lu par tout rattachement actif (§3.12).
      title: 'Facturation',
      items: [{ title: 'Factures', url: '/portail/factures', icon: FileText }],
    },
  ],
}
