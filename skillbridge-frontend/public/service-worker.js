// Empty service worker to prevent 404 errors
// This file is created to stop browser requests for /service-worker.js

self.addEventListener("install", function () {
  // Immediately activate the service worker
  self.skipWaiting();
});

self.addEventListener("activate", function (event) {
  // Take control of all pages
  event.waitUntil(self.clients.claim());
});
