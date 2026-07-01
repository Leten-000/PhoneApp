const CACHE_NAME = 'jarvis-cache-v2';
const CORE_ASSETS = ['.', 'index.html', 'manifest.webmanifest', 'icon.svg', 'styles.css'];

const toScopeUrl = (assetPath) => new URL(assetPath, self.registration.scope).toString();

self.addEventListener('install', (event) => {
  self.skipWaiting();
  event.waitUntil(
    caches
      .open(CACHE_NAME)
      .then((cache) => cache.addAll(CORE_ASSETS.map(toScopeUrl))),
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches
      .keys()
      .then((cacheNames) => Promise.all(cacheNames.map((cacheName) => {
        if (cacheName !== CACHE_NAME) {
          return caches.delete(cacheName);
        }

        return undefined;
      })))
      .then(() => self.clients.claim()),
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') {
    return;
  }

  event.respondWith(
    fetch(event.request)
      .then((response) => {
        const responseClone = response.clone();
        caches.open(CACHE_NAME).then((cache) => cache.put(event.request, responseClone));
        return response;
      })
      .catch(() => caches.match(event.request).then((cachedResponse) => cachedResponse || caches.match(toScopeUrl('index.html')))),
  );
});
