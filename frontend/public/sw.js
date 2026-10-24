// Service worker minimal : il rend l'application installable, sans mode hors ligne. Rien n'est mis en cache, sauf la
// page « Pas de connexion », affichée à la place d'un écran quand le réseau manque. Il affiche aussi les notifications
// push (spec 11) et ouvre leur lien.
const CACHE = 'bnia-hors-ligne-1'
const PAGE_HORS_LIGNE = '/hors-ligne.html'

self.addEventListener('install', (evenement) => {
  evenement.waitUntil(caches.open(CACHE).then((cache) => cache.add(PAGE_HORS_LIGNE)))
  self.skipWaiting()
})

self.addEventListener('activate', (evenement) => {
  evenement.waitUntil(
    caches
      .keys()
      .then((cles) => Promise.all(cles.filter((cle) => cle !== CACHE).map((cle) => caches.delete(cle))))
      .then(() => self.clients.claim())
  )
})

// Seules les ouvertures de page passent par ici, et toujours par le réseau ; le reste (API, scripts, fichiers) ne
// passe pas par le service worker.
self.addEventListener('fetch', (evenement) => {
  if (evenement.request.mode !== 'navigate' || evenement.request.method !== 'GET') return
  evenement.respondWith(fetch(evenement.request).catch(() => caches.match(PAGE_HORS_LIGNE)))
})

// Notification push (spec 11) : { titre, texte, lien }, déchiffrée par le navigateur (WebPushHttp côté serveur). Le
// lien sert d'étiquette : une nouvelle alerte sur le même contrat remplace la précédente, et sonne de nouveau.
self.addEventListener('push', (evenement) => {
  const message = evenement.data ? evenement.data.json() : {}
  evenement.waitUntil(
    self.registration.showNotification(message.titre || 'BNIA', {
      body: message.texte || '',
      icon: '/icones/icone-192.png',
      badge: '/icones/icone-192.png',
      lang: 'fr',
      tag: message.lien || 'bnia',
      renotify: true,
      data: { lien: message.lien || '/' },
    })
  )
})

// Un appui ouvre le lien : dans une fenêtre de l'application déjà ouverte, mise au premier plan, sinon (ou si elle
// refuse de naviguer) dans une nouvelle.
self.addEventListener('notificationclick', (evenement) => {
  evenement.notification.close()
  const url = new URL(evenement.notification.data?.lien || '/', self.location.origin).href
  evenement.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((fenetres) => {
      const fenetre = fenetres.find((f) => new URL(f.url).origin === self.location.origin)
      if (!fenetre) return self.clients.openWindow(url)
      return fenetre
        .focus()
        .then((f) => (f || fenetre).navigate(url))
        .catch(() => self.clients.openWindow(url))
    })
  )
})
