import { type LinkProps } from '@tanstack/react-router'

type BaseNavItem = {
  title: string
  badge?: string
  icon?: React.ElementType
}

type NavLink = BaseNavItem & {
  url: LinkProps['to'] | (string & {})
  items?: never
}

type NavCollapsible = BaseNavItem & {
  items: (BaseNavItem & { url: LinkProps['to'] | (string & {}) })[]
  url?: never
}

type NavItem = NavCollapsible | NavLink

type NavGroup = {
  title: string
  items: NavItem[]
}

/** Un espace de l'application : le back-office BNIA ou le portail opérateur. */
type Espace = {
  nom: string
  description: string
  url: LinkProps['to']
  navGroups: NavGroup[]
  /** Pages hors de la barre latérale, proposées par la recherche. */
  autresPages?: NavGroup[]
}

export type { Espace, NavGroup, NavItem, NavCollapsible, NavLink }
