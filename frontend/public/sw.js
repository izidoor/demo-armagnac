// Service worker minimal : il rend l'application installable, sans mode hors ligne. Rien n'est mis en cache, sauf la
// page « Pas de connexion », affichée à la place d'un écran quand le réseau manque.
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
