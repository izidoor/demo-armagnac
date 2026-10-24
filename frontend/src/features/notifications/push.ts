import {
  abonnerAppareil,
  clePubliqueNotifications,
  desabonnerAppareil,
} from '@/api/generated'

/**
 * Notifications push de cet appareil (spec 11, Web Push) : l'abonnement est donné par le service push du navigateur,
 * puis enregistré par le serveur, qui y envoie les alertes des contrats et des retiraisons (RG-CA-18).
 */
export type EtatPush =
  /** L'installation n'a pas de clés, ou le navigateur ne connaît pas le push. */
  | 'indisponible'
  /** iPhone ou iPad : le push n'existe que pour l'application ajoutée à l'écran d'accueil (iOS 16.4 et plus). */
  | 'a-installer'
  /** L'utilisateur a refusé : seul le réglage du navigateur ou du téléphone peut les rétablir. */
  | 'refusees'
  | 'activees'
  | 'desactivees'

export async function lireClePublique(): Promise<string | null> {
  const r = await clePubliqueNotifications({ throwOnError: true })
  return r.data.clePublique ?? null
}

function pushPrisEnCharge(): boolean {
  return (
    'serviceWorker' in navigator &&
    'PushManager' in window &&
    'Notification' in window
  )
}

/** iPhone, ou iPad (qui se présente comme un Mac tactile), dans Safari plutôt que depuis l'icône de l'écran d'accueil. */
function iosHorsEcranAccueil(): boolean {
  const ios =
    /iPad|iPhone|iPod/.test(navigator.userAgent) ||
    (/Macintosh/.test(navigator.userAgent) && navigator.maxTouchPoints > 1)
  const installee =
    window.matchMedia('(display-mode: standalone)').matches ||
    (navigator as Navigator & { standalone?: boolean }).standalone === true
  return ios && !installee
}

async function abonnementActuel(): Promise<PushSubscription | null> {
  const enregistrement = await navigator.serviceWorker.getRegistration()
  return (await enregistrement?.pushManager.getSubscription()) ?? null
}

async function enregistrer(abonnement: PushSubscription): Promise<void> {
  const json = abonnement.toJSON()
  await abonnerAppareil({
    body: {
      adresse: abonnement.endpoint,
      cleP256dh: json.keys?.p256dh ?? '',
      secretAuth: json.keys?.auth ?? '',
    },
    throwOnError: true,
  })
}

/**
 * L'état de cet appareil. Déjà abonné, l'abonnement est réenregistré : il passe ainsi à l'utilisateur connecté si un
 * autre s'y était abonné sans se déconnecter (RG-NOT-02).
 */
export async function lireEtat(cle: string | null): Promise<EtatPush> {
  if (iosHorsEcranAccueil()) return 'a-installer'
  if (!pushPrisEnCharge() || !cle) return 'indisponible'
  if (Notification.permission === 'denied') return 'refusees'
  const abonnement = await abonnementActuel()
  if (abonnement && Notification.permission === 'granted') {
    await enregistrer(abonnement)
    return 'activees'
  }
  return 'desactivees'
}

/** La clé VAPID, de base64url en octets, pour {@code pushManager.subscribe}. */
function octets(base64url: string): Uint8Array<ArrayBuffer> {
  const base64 = base64url.replace(/-/g, '+').replace(/_/g, '/')
  const brut = atob(base64 + '='.repeat((4 - (base64.length % 4)) % 4))
  return Uint8Array.from(brut, (c) => c.charCodeAt(0))
}

/** À appeler sur un appui : iOS ne demande l'autorisation qu'en réponse à un geste de l'utilisateur. */
export async function activer(cle: string): Promise<EtatPush> {
  const permission = await Notification.requestPermission()
  if (permission !== 'granted')
    return permission === 'denied' ? 'refusees' : 'desactivees'
  const enregistrement = await navigator.serviceWorker.ready
  const abonnement =
    (await enregistrement.pushManager.getSubscription()) ??
    (await enregistrement.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: octets(cle),
    }))
  await enregistrer(abonnement)
  return 'activees'
}

export async function desactiver(): Promise<EtatPush> {
  const abonnement = await abonnementActuel()
  if (abonnement) {
    await desabonnerAppareil({
      body: { adresse: abonnement.endpoint },
      throwOnError: true,
    })
    await abonnement.unsubscribe()
  }
  return 'desactivees'
}

/**
 * À la déconnexion, avant de fermer la session : cet appareil ne reçoit plus les alertes de ce compte (RG-NOT-05).
 * Sans effet si le push n'est pas activé ; un échec n'empêche pas la déconnexion.
 */
export async function desabonnerCetAppareil(): Promise<void> {
  try {
    if (pushPrisEnCharge()) await desactiver()
  } catch {
    // La déconnexion passe avant : l'abonnement expirera ou sera repris au prochain abonnement (RG-NOT-02, 04).
  }
}
