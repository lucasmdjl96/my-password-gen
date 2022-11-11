const precache = "precache-$VERSION";
const runtime = "runtime";
const cachesUsed = [precache, runtime];

const precacheFiles = [
    "/static/js/my-password-gen.js",
    "/",
    "/static/css/style.css",
];

async function openAndAddFiles() {
    const precacheCache = await caches.open(precache);
    return await precacheCache.addAll(precacheFiles);
}

self.addEventListener("install", event => {
    event.waitUntil(
        openAndAddFiles()
    );
})

async function deleteOldCaches() {
    const cacheNames = await caches.keys();
    const cachesToDelete = cacheNames.filter(cacheName => {
        return !cachesUsed.includes(cacheName);
    });
    return await Promise.all(cachesToDelete.map(cacheToDelete => {
        return caches.delete(cacheToDelete);
    }));
}

/*async function enableNavigationPreload() {
    if (self.registration.navigationPreload) {
        await self.registration.navigationPreload.enable();
    }
}*/

self.addEventListener("activate", event => {
    event.waitUntil(
        deleteOldCaches()
    );
    /*event.waitUntil(
        enableNavigationPreload()
    );*/
});

async function deleteCache() {
    await Promise.all(cachesUsed.map(cache => {
        return caches.delete(cache);
    }));
    return new Response(null, {
        status: 200
    });
}

async function cacheFirst(event) {
    const precacheCache = await caches.open(precache);
    const cacheResponse = await precacheCache.match(event.request);
    if (cacheResponse) {
        return cacheResponse;
    }

    /*const preloadResponse = await event.preloadResponse;
    if (preloadResponse) {
        return preloadResponse;
    }*/

    return await fetch(event.request);
}

async function staleWhileRevalidate(event) {
    const cacheResponse = await caches.match(event.request);
    if (cacheResponse) {
        fetch(event.request).then(response => {
            return addToRuntime(event.request, response)
        });
        return cacheResponse;
    }
    return await fetch(event.request).then(response => {
        return addToRuntime(event.request, response)
    });
}

async function addToRuntime(request, response) {
    const runtimeCache = await caches.open(runtime);
    await runtimeCache.put(request, response.clone());
    return response;
}

self.addEventListener("fetch", event => {
    if (event.request.url === `${self.location.origin}/deleteCache`) {
        event.respondWith(
            deleteCache()
        );
    } else if (event.request.url.startsWith(self.location.origin)) {
        event.respondWith(
            cacheFirst(event)
        );
    } else {
        event.respondWith(
            staleWhileRevalidate(event)
        );
    }
});
