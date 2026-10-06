import { type LinkProps } from '@tanstack/react-router'

type User = {
  name: string
  email: string
  avatar: string
}

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
  user: User
  navGroups: NavGroup[]
}

export type { Espace, NavGroup, NavItem, NavCollapsible, NavLink }
