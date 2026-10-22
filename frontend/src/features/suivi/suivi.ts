import { client } from '@/api/generated/client.gen'

/**
 * Suivi de l'utilisation du prototype, pour son auteur seul (backend : `fr.bnia.boot.suivi`, lecture par `suivi.sh`) :
 * chaque écran affiché du back-office et du portail, chaque section de « Voir l'offre », et la présence sur la page,
 * signalée chaque minute tant que l'onglet est visible, d'où le temps passé. Les téléchargements et imports sont relevés
 * par le serveur.
 *
 * `?suivi=non` sur n'importe quelle adresse retire ce navigateur du suivi (`?suivi=oui` l'y remet) : les comptes de
 * démonstration sont partagés, l'e-mail seul ne distingue pas l'auteur de ses visiteurs.
 */

type TypeEvenement = 'ECRAN' | 'OFFRE' | 'PRESENCE'
type Appareil = 'ORDINATEUR' | 'TABLETTE' | 'MOBILE'

/** Lu aussi par le serveur (`SuiviFichiers`). */
const COOKIE = 'bnia-suivi'
const UN_AN = 365 * 24 * 3600
const BATTEMENT = 60_000
const ESPACES = /^\/(bo|portail)(\/|$)/

/** Gabarit de la route affichée (`/bo/operateurs/$operateurId`), hors back-office et portail : null. */
let route: string | null = null
/** Section de l'offre affichée, tant que la fenêtre est ouverte. */
let section: string | null = null
let chemin: string | null = null

function exclu(): boolean {
  return document.cookie.split('; ').includes(`${COOKIE}=non`)
}

function appareil(): Appareil {
  const ua = navigator.userAgent
  // iPadOS se présente comme un Mac : seul l'écran tactile le trahit.
  const macTactile = /Macintosh/.test(ua) && navigator.maxTouchPoints > 1
  if (
    /iPad|Tablet/.test(ua) ||
    macTactile ||
    (/Android/.test(ua) && !/Mobile/.test(ua))
  )
    return 'TABLETTE'
  if (/Mobi|iPhone|Android/.test(ua)) return 'MOBILE'
  return 'ORDINATEUR'
}

function lieu(): string | null {
  if (route === null) return null
  return section === null ? route : `offre/${section}`
}

function signaler(type: TypeEvenement, ou: string | null) {
  if (ou === null || exclu()) return
  client
    .post({
      url: '/api/suivi/evenements',
      body: { type, lieu: ou, appareil: appareil() },
    })
    .catch(() => {})
}

/**
 * Retient `?suivi=non` ou `?suivi=oui` de l'adresse, dans un cookie : le serveur l'écarte aussi des téléchargements et
 * imports qu'il relève lui-même.
 */
function lireConsigne(recherche: string) {
  const consigne = new URLSearchParams(recherche).get('suivi')
  if (consigne === 'non')
    document.cookie = `${COOKIE}=non; path=/; max-age=${UN_AN}; samesite=lax`
  if (consigne === 'oui') document.cookie = `${COOKIE}=; path=/; max-age=0`
}

/** Un écran résolu par le routeur ; un simple changement de filtre (même chemin) ne compte pas. */
export function ecranAffiche(
  pathname: string,
  search: string,
  gabarit: string
) {
  lireConsigne(search)
  if (pathname === chemin) return
  chemin = pathname
  route = ESPACES.test(pathname) ? gabarit.replace(/(.)\/$/, '$1') : null
  signaler('ECRAN', route)
}

/** La section affichée par « Voir l'offre » ; null à la fermeture, le lieu redevient l'écran. */
export function sectionAffichee(id: string | null) {
  if (id === section) return
  section = id
  if (id !== null) signaler('OFFRE', lieu())
}

let battement: number | undefined

/** Retient la consigne de l'adresse, puis signale la présence chaque minute, onglet visible. Une seule fois. */
export function demarrerSuivi() {
  if (battement !== undefined) return
  // Lue aussi au chargement : une route qui valide sa recherche peut écarter `suivi`.
  lireConsigne(window.location.search)
  battement = window.setInterval(() => {
    if (document.visibilityState === 'visible') signaler('PRESENCE', lieu())
  }, BATTEMENT)
}
